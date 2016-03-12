package search

import common.io.{DirectoryRef, Root}
import models.{Album, Artist, MusicFinder, Song}
import org.hamcrest.{BaseMatcher, Description}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FreeSpec, OneInstancePerTest}

import scala.collection.mutable

class MetadataCacherTest extends FreeSpec with ShouldMatchers with OneInstancePerTest with MockitoSugar {
  val root = new Root
  val pathToSongs = mutable.HashMap[String, Song]()
  val fakeMf = new MusicFinder {
    override val extensions: List[String] = List("mp3")
    override val dir = root
    override val subDirs: List[String] = null
    override def albumDirs: Seq[DirectoryRef] = dir.dirs
  }
  private val saver = mock[JsonableSaver]
  val $ = new MetadataCacher(fakeMf, pathToSongs, saver)
  def addSong(s: Song) = {
    val dir = root addSubDir s.album.title
    val file = dir addFile s.file.getName
    pathToSongs += file.path -> s
    dir
  }

  def artistFromAlbum(album: Album) = new Artist(album.artistName, Set(album))
  def matches[T](t: TraversableOnce[T]): TraversableOnce[T] = argThat(new BaseMatcher[TraversableOnce[T]] {
    override def matches(item: scala.Any): Boolean = item match {
      case s: TraversableOnce[T] => t.toSet == s.toSet
      case _ => false
    }
    override def describeTo(description: Description) {
      description.appendText(t.toString)
    }
  })
  def verifyData[T: Jsonable](xs: T*) {
    verify(saver).save(matches(xs))(any(), any())
  }
  "index" - {
    "all" in {
      val album = Models.mockAlbum()
      val song = Models.mockSong(album = album)
      addSong(song)
      $.indexAll()
      verifyData(song)
      verifyData(album)
      verifyData(artistFromAlbum(album))
    }
    "no album duplicates" in {
      val album = Models.mockAlbum()
      val song1 = Models.mockSong(title = "song1", album = album)
      val song2 = Models.mockSong(title = "song2", album = album)
      addSong(song1)
      addSong(song2)
      $.indexAll()
      verifyData(album)
    }
    "no artist duplicates" in {
      val album1 = Models.mockAlbum(title = "a1")
      val album2 = Models.mockAlbum(title = "a2")
      val song1 = Models.mockSong(title = "song1", album = album1)
      val song2 = Models.mockSong(title = "song2", album = album2)
      addSong(song1)
      addSong(song2)
      $.indexAll()
      verifyData(new Artist(song1.artistName, Set(album1, album2)))
    }
  }
  "incremental" in {
    val album1 = Models.mockAlbum(title = "album1")
    val song1 = Models.mockSong(title = "song1", album = album1, artistName = "artist1")
    $(addSong(song1))
    verifyData(song1)
    verifyData(album1)
    verifyData(artistFromAlbum(album1))
    val album2 = Models.mockAlbum(title = "album2")
    val song2 = Models.mockSong(title = "song2", album = album2, artistName = "artist1")
    $(addSong(song2))
    verifyData(song1)
    verifyData(album1)
    verifyData(artistFromAlbum(album1).copy(_albums = Set(album1, album2)))
    val album3 = Models.mockAlbum(title = "album3")
    val song3 = Models.mockSong(title = "song3", album = album3, artistName = "artist2")
    $(addSong(song3))
    verifyData(song1)
    verifyData(album1)
    verifyData(artistFromAlbum(album1).copy(_albums = Set(album1, album2)), artistFromAlbum(album3))
  }
}
