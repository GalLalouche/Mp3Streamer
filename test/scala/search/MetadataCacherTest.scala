package search

import java.util.concurrent.{LinkedBlockingQueue, TimeUnit}

import backend.configs.{Configuration, TestConfiguration}
import common.AuxSpecs
import common.io.{DirectoryRef, MemoryRoot}
import models.{Album, Artist, MusicFinder, Song}
import org.hamcrest.{BaseMatcher, Description}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FreeSpec, Matchers, OneInstancePerTest}
import search.MetadataCacher.IndexUpdate

import scala.collection.mutable
import scala.concurrent.{Await, ExecutionContext, Promise}

class MetadataCacherTest extends FreeSpec with OneInstancePerTest with MockitoSugar with Matchers with AuxSpecs {
  private implicit val root = new MemoryRoot
  implicit val c = TestConfiguration().copy(_root = root, _mf = new MusicFinder {
    override val extensions: List[String] = List("mp3")
    override val dir = root
    override val subDirs: List[String] = null
    override def albumDirs: Seq[DirectoryRef] = dir.dirs
  })
  private val pathToSongs = mutable.HashMap[String, Song]()
  private def fromSaver(saver: JsonableSaver)(implicit c: Configuration) = new MetadataCacher(saver) {
    override protected def parseSong(filePath: String): Song = pathToSongs(filePath)
  }
  private def addSong(s: Song) = {
    val dir = root addSubDir s.album.title
    val file = dir addFile s.file.getName
    pathToSongs += file.path -> s
    dir
  }

  "index" - {
    val mockSaver = mock[JsonableSaver]
    val $ = fromSaver(mockSaver)
    // TODO replace mock with fake
    def verifyData[T: Jsonable](xs: T*) {
      def matches(t: TraversableOnce[T]): TraversableOnce[T] = argThat(
        new BaseMatcher[TraversableOnce[T]] {
          override def matches(item: scala.Any): Boolean = item match {
            case s: TraversableOnce[_] => t.toSet == s.toSet
            case _ => false
          }
          override def describeTo(description: Description) {
            description.appendText(t.toString)
          }
        }
      )
      verify(mockSaver).save(matches(xs))(any(), any())
    }
    def indexAllAndWaitForCompletion() = {
      val p = Promise[Unit]
      $.indexAll() doOnCompleted (p success Unit) subscribe()
      import scala.concurrent.duration.DurationInt
      Await.ready(p.future, 1 second)
    }

    "all" in {
      val album = Models.mockAlbum()
      val song = Models.mockSong(album = album)
      addSong(song)
      indexAllAndWaitForCompletion()
      verifyData(song)
      verifyData(album)
      verifyData(Artist(album.artistName, Set(album)))
    }
    val album = Models.mockAlbum()
    "no album duplicates" in {
      val song1 = Models.mockSong(title = "song1", album = album)
      val song2 = Models.mockSong(title = "song2", album = album)
      addSong(song1)
      addSong(song2)
      indexAllAndWaitForCompletion()
      verifyData(album)
    }
    "no artist duplicates" in {
      val album1 = Models.mockAlbum(title = "a1")
      val album2 = Models.mockAlbum(title = "a2")
      val song1 = Models.mockSong(title = "song1", album = album1)
      val song2 = Models.mockSong(title = "song2", album = album2)
      addSong(song1)
      addSong(song2)
      indexAllAndWaitForCompletion()
      verifyData(Artist(song1.artistName, Set(album1, album2)))
    }
    "with sink" in {
      val album1 = Models.mockAlbum(title = "a1")
      val album2 = Models.mockAlbum(title = "a2")
      val song1 = Models.mockSong(title = "song1", album = album1)
      val song2 = Models.mockSong(title = "song2", album = album2)
      addSong(song1)
      addSong(song2)
      val q = new LinkedBlockingQueue[IndexUpdate]()
      $.indexAll().subscribe(onNext = q put _)
      def matchWithIndices(current: Int, total: Int) = new Matcher[IndexUpdate] {
        override def apply(left: IndexUpdate): MatchResult = {
          MatchResult(
            left.currentIndex == current && left.totalNumber == total,
            s"$left did not have currentIndex of $current and totalNumber of $total",
            s"$left did have currentIndex of $current and totalNumber of $total")
        }
      }
      q.poll(5, TimeUnit.SECONDS).ensuring(_ != null, "Did not receive a metadata update") should matchWithIndices(1, 2)
      q.poll(5, TimeUnit.SECONDS).ensuring(_ != null, "Did not receive a metadata update") should matchWithIndices(2, 2)
    }
    "with blocking sink" in {
      val $ = fromSaver(mockSaver)(c.copy(_ec = ExecutionContext.Implicits.global))
      val album1 = Models.mockAlbum(title = "a1")
      val song1 = Models.mockSong(title = "song1", album = album1)
      addSong(song1)
      $.indexAll().subscribe { _ => while (true) {} }
      Thread.sleep(100)
      verifyData(song1)
    }
  }

  "incremental (integration)" in {
    // it's a pain in the ass to test for updates using mocks
    val saver = new JsonableSaver
    val $ = fromSaver(saver)
    val album1 = Models.mockAlbum(title = "album1")
    val song1 = Models.mockSong(title = "song1", album = album1, artistName = "artist1")
    $(addSong(song1))
    saver.load[Song] shouldReturn Seq(song1)
    saver.load[Album] shouldReturn Seq(album1)
    saver.load[Artist] shouldReturn Seq(Artist("artist1", Set(album1)))
    val album2 = Models.mockAlbum(title = "album2")
    val song2 = Models.mockSong(title = "song2", album = album2, artistName = "artist1")
    $(addSong(song2))

    saver.load[Song].toSet shouldReturn Set(song1, song2)
    saver.load[Album].toSet shouldReturn Set(album1, album2)
    saver.load[Artist].toSet shouldReturn Set(Artist("artist1", Set(album1, album2)))
    val album3 = Models.mockAlbum(title = "album3")
    val song3 = Models.mockSong(title = "song3", album = album3, artistName = "artist2")
    $(addSong(song3))
    saver.load[Song].toSet shouldReturn Set(song1, song2, song3)
    saver.load[Album].toSet shouldReturn Set(album1, album2, album3)
    saver.load[Artist].toSet shouldReturn Set(Artist("artist1", Set(album1, album2)), Artist("artist2", Set(album3)))
  }
}
