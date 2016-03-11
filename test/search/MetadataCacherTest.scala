package search

import common.io.Root
import common.rich.path.Directory
import models.{Album, Artist, MusicFinder, Song}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FreeSpec, OneInstancePerTest}
import search.Jsonable._

import scala.collection.mutable

class MetadataCacherTest extends FreeSpec with ShouldMatchers with OneInstancePerTest with MockitoSugar {
  val $ = new MetadataCacher(new Root) {
    override protected def getSong(path: String): Song = pathToSongs(path)
  }
  val pathToSongs = mutable.HashMap[String, Song]()
  val fakeMf = new MusicFinder {
    override val extensions: List[String] = null
    override val dir = null
    override val subDirs: List[String] = null
    override def getSongFilePaths: IndexedSeq[String] = pathToSongs.keys.toVector
  }
  "save" - {
    def test[T: Jsonable](t: T)(implicit m: Manifest[T]) {
      val seq = Seq(t)
      $.save(seq)
      $.load[T] should be === seq
    }
    "songs" in {
      test(Models.mockSong())
    }
    "albums" in {
      test(Models.mockAlbum())
    }
    "artists" in {
      test(Models.mockArtist())
    }
  }
  "index" - {
    "all" in {
      val album = Models.mockAlbum()
      val song = Models.mockSong(album = album)
      pathToSongs += "path" -> song
      $.indexAll(fakeMf)
      $.load[Song] should be === Seq(song)
      $.load[Album] should be === Seq(album)
      $.load[Artist] should be === Seq(new Artist(song.artistName, Set(album)))
    }
    "no album duplicates" in {
      val album = Models.mockAlbum()
      val song1 = Models.mockSong(title = "song1", album = album)
      val song2 = Models.mockSong(title = "song2", album = album)
      pathToSongs += "s1" -> song1 += "s2" -> song2
      $.indexAll(fakeMf)
      $.load[Album].size should be === 1
    }
    "no artist duplicates" in {
      val album1 = Models.mockAlbum(title = "a1")
      val album2 = Models.mockAlbum(title = "a2")
      val song1 = Models.mockSong(title = "song1", album = album1)
      val song2 = Models.mockSong(title = "song2", album = album2)
      pathToSongs += "s1" -> song1 += "s2" -> song2
      $.indexAll(fakeMf)
      $.load[Album].size should be === 2
      $.load[Artist].size should be === 1
    }
  }
  "incremental" in {
    val album1 = Models.mockAlbum()
    val song1 = Models.mockSong(file = "song1", title = "song1", album = album1, artistName = "artist1")
    pathToSongs += "song1" -> song1
    $(Seq("song1"))
    $.load[Song] should be === Seq(song1)
    $.load[Album] should be === Seq(album1)
    $.load[Artist] should be === Seq(new Artist("artist1", Set(album1)))
    val album2 = Models.mockAlbum(title = "album2")
    val song2 = Models.mockSong(file = "song2", title = "song2", album = album2, artistName = "artist1")
    val album3 = Models.mockAlbum(title = "album3")
    val song3 = Models.mockSong(file = "song3", title = "song3", album = album3, artistName = "artist2")
    pathToSongs += "song2" -> song2 += "song3" -> song3
    $(Seq("song2"))
    $.load[Song].toSet should be === Set(song1, song2)
    $.load[Album].toSet should be === Set(album1, album2)
    $.load[Artist].toSet should be === Set(new Artist("artist1", Set(album1, album2)))
    $(Seq("song3"))
    $.load[Song].toSet should be === Set(song1, song2, song3)
    $.load[Album].toSet should be === Set(album1, album2, album3)
    $.load[Artist].toSet should be === Set(new Artist("artist1", Set(album1, album2)), new Artist("artist2", Set(album3)))
  }
}
