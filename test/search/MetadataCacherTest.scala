package search

import java.util.concurrent.{LinkedBlockingQueue, Semaphore, TimeUnit}

import common.io.{DirectoryRef, Root}
import models.{Album, Artist, MusicFinder, Song}
import org.hamcrest.{BaseMatcher, Description}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FreeSpec, Matchers, OneInstancePerTest, ShouldMatchers}
import search.MetadataCacher.IndexUpdate

import scala.collection.mutable

class MetadataCacherTest extends FreeSpec with OneInstancePerTest with MockitoSugar with Matchers with ShouldMatchers {
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
  def matches[T](t: TraversableOnce[T]): TraversableOnce[T] = argThat(
    new BaseMatcher[TraversableOnce[T]] {
      override def matches(item: scala.Any): Boolean = item match {
        case s: TraversableOnce[T] => t.toSet == s.toSet
        case _ => false
      }
      override def describeTo(description: Description) {
        description.appendText(t.toString)
      }
    }
  )
  def verifyData[T: Jsonable](xs: T*) {
    verify(saver).save(matches(xs))(any(), any())
  }
  "index" - {
    def indexAll() = {
      val lock = new Semaphore(0)
      $.indexAll().subscribe(_ => (), _ => (), () => lock.release())
      lock.acquire()
    }
    "all" in {
      val album = Models.mockAlbum()
      val song = Models.mockSong(album = album)
      addSong(song)
      indexAll()
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
      indexAll()
      verifyData(album)
    }
    "no artist duplicates" in {
      val album1 = Models.mockAlbum(title = "a1")
      val album2 = Models.mockAlbum(title = "a2")
      val song1 = Models.mockSong(title = "song1", album = album1)
      val song2 = Models.mockSong(title = "song2", album = album2)
      addSong(song1)
      addSong(song2)
      indexAll()
      verifyData(new Artist(song1.artistName, Set(album1, album2)))
    }
    "with sink" in {
      val album1 = Models.mockAlbum(title = "a1")
      val album2 = Models.mockAlbum(title = "a2")
      val song1 = Models.mockSong(title = "song1", album = album1)
      val song2 = Models.mockSong(title = "song2", album = album2)
      addSong(song1)
      addSong(song2)
      val q = new LinkedBlockingQueue[IndexUpdate]()
      $.indexAll.subscribe(q.put(_))
      def matchWithIndices(current: Int, total: Int) = new Matcher[IndexUpdate] {
        override def apply(left: IndexUpdate): MatchResult =
          MatchResult.apply(
            left.currentIndex == current && left.totalNumber == total,
            s"$left did not have currentIndex of $current and totalNumber of $total",
            s"$left did have currentIndex of $current and totalNumber of $total")
      }
      println(q)
      q.poll(1, TimeUnit.SECONDS) should matchWithIndices(1, 2)
      println(q)
      q.poll(1, TimeUnit.SECONDS) should matchWithIndices(2, 2)
    }
    "with blocking sink" in {
      val album1 = Models.mockAlbum(title = "a1")
      val song1 = Models.mockSong(title = "song1", album = album1)
      addSong(song1)
      $.indexAll.subscribe { _ => while(true){} }
      Thread.sleep(100)
      verifyData(song1)
    }
  }

  "incremental (integration)" in {
    // it's a pain in the ass to test for updates using mocks
    val saver = new JsonableSaver(root)
    val $ = new MetadataCacher(fakeMf, pathToSongs, saver)
    val album1 = Models.mockAlbum(title = "album1")
    val song1 = Models.mockSong(title = "song1", album = album1, artistName = "artist1")
    $(addSong(song1))
    saver.load[Song] should be === Seq(song1)
    saver.load[Album] should be === Seq(album1)
    saver.load[Artist] should be === Seq(new Artist("artist1", Set(album1)))
    val album2 = Models.mockAlbum(title = "album2")
    val song2 = Models.mockSong(title = "song2", album = album2, artistName = "artist1")
    $(addSong(song2))

    saver.load[Song].toSet should be === Set(song1, song2)
    saver.load[Album].toSet should be === Set(album1, album2)
    saver.load[Artist].toSet should be === Set(new Artist("artist1", Set(album1, album2)))
    val album3 = Models.mockAlbum(title = "album3")
    val song3 = Models.mockSong(title = "song3", album = album3, artistName = "artist2")
    $(addSong(song3))
    saver.load[Song].toSet should be === Set(song1, song2, song3)
    saver.load[Album].toSet should be === Set(album1, album2, album3)
    saver.load[Artist].toSet should be === Set(new Artist("artist1", Set(album1, album2)), new Artist("artist2", Set(album3)))
  }
}
