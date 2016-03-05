package search

import common.AuxSpecs
import common.io.MemoryFileSystem
import common.rich.path.Directory
import models.{Album, Artist, MusicFinder, Song}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FreeSpec, OneInstancePerTest}
import search.Jsonable._

import scala.collection.mutable

class MetadataCacherTest extends FreeSpec with AuxSpecs with OneInstancePerTest with MockitoSugar {
  val $ = new MetadataCacher with MemoryFileSystem {
    override protected def getSong(path: String): Song = pathToSongs(path)
  }
  val pathToSongs = mutable.HashMap[String, Song]()
  val fakeMf = new MusicFinder {
    override val extensions: List[String] = null
    override val dir: Directory = null
    override val subDirs: List[String] = null
    override def getSongFilePaths: IndexedSeq[String] = pathToSongs.keys.toVector
  }
  "save" - {
    def test[T: Jsonable](t: T)(implicit m: Manifest[T]) {
      val seq = Seq(t)
      $.save(seq)
      $.load[T] shouldReturn seq
    }
    "songs" in { test(Models.mockSong()) }
    "albums" in { test(Models.mockAlbum()) }
    "artists" in { test(Models.mockArtist()) }
  }
  "index" - {
    "all" in {
      val album = Models.mockAlbum()
      val song = Models.mockSong(album = album)
      pathToSongs += "path" -> song
      $.indexAll(fakeMf)
      $.load[Song] shouldReturn Seq(song)
      $.load[Album] shouldReturn Seq(album)
      $.load[Artist] shouldReturn Seq(new Artist(song.artistName, Set(album)))
    }
    "no album duplicates" in {
      val album = Models.mockAlbum()
      val song1 = Models.mockSong(title = "song1", album = album)
      val song2 = Models.mockSong(title = "song2", album = album)
      pathToSongs += "s1" -> song1 += "s2" -> song2
      $.indexAll(fakeMf)
      $.load[Album].size shouldReturn 1
    }
    "no artist duplicates" in {
      val album1 = Models.mockAlbum(title = "a1")
      val album2 = Models.mockAlbum(title = "a2")
      val song1 = Models.mockSong(title = "song1", album = album1)
      val song2 = Models.mockSong(title = "song2", album = album2)
      pathToSongs += "s1" -> song1 += "s2" -> song2
      $.indexAll(fakeMf)
      $.load[Album].size shouldReturn 2
      $.load[Artist].size shouldReturn 1
    }
  }
  "incremental" in {
    val album1 = Models.mockAlbum()
    val song1 = Models.mockSong(file = "song1", title = "song1", album = album1, artistName = "artist1")
    pathToSongs += "song1" -> song1
    $(Seq("song1"))
    $.load[Song] shouldReturn Seq(song1)
    $.load[Album] shouldReturn Seq(album1)
    $.load[Artist] shouldReturn Seq(new Artist("artist1", Set(album1)))
    val album2 = Models.mockAlbum(title = "album2")
    val song2 = Models.mockSong(file = "song2", title = "song2", album = album2, artistName = "artist1")
    val album3 = Models.mockAlbum(title = "album3")
    val song3 = Models.mockSong(file = "song3", title = "song3", album = album3, artistName = "artist2")
    pathToSongs += "song2" -> song2 += "song3" -> song3
    $(Seq("song2"))
    $.load[Song].toSet shouldReturn Set(song1, song2)
    $.load[Album].toSet shouldReturn Set(album1, album2)
    $.load[Artist].toSet shouldReturn Set(new Artist("artist1", Set(album1, album2)))
    $(Seq("song3"))
    $.load[Song].toSet shouldReturn Set(song1, song2, song3)
    $.load[Album].toSet shouldReturn Set(album1, album2, album3)
    $.load[Artist].toSet shouldReturn Set(new Artist("artist1", Set(album1, album2)), new Artist("artist2", Set(album3)))
  }
}
