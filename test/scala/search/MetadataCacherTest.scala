package search

import java.util.concurrent.{LinkedBlockingQueue, TimeUnit}

import backend.configs.{Configuration, TestConfiguration}
import common.io.{DirectoryRef, JsonableSaver, MemoryRoot}
import common.{AuxSpecs, Jsonable}
import models.{Album, Artist, MusicFinder, Song}
import org.hamcrest.{BaseMatcher, Description}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FreeSpec, Matchers, OneInstancePerTest}
import rx.lang.scala.Observable
import search.MetadataCacher.IndexUpdate
import search.ModelsJsonable._

import scala.collection.mutable
import scala.concurrent.{Await, Promise}

class MetadataCacherTest extends FreeSpec with OneInstancePerTest with MockitoSugar with Matchers with AuxSpecs {
  private implicit val root = new MemoryRoot
  val songs = root.addSubDir("songs")
  implicit val c = TestConfiguration().copy(_root = root, _mf = new MusicFinder {
    override val extensions = Set("mp3")
    override val dir = songs
    override val subDirs: List[String] = null
    override def albumDirs = dir.dirs
  })
  private val pathToSongs = mutable.HashMap[String, Song]()
  private def fromSaver(saver: JsonableSaver)(implicit c: Configuration) = new MetadataCacher(saver) {
    override protected def parseSong(filePath: String): Song = pathToSongs(filePath)
  }
  private def addSong(s: Song) = {
    val dir = songs addSubDir s.album.title
    val file = dir addFile s.file.getName
    pathToSongs += file.path -> s
    dir
  }

  def awaitCompletion($: Observable[Any]) = {
    val p = Promise[Unit]
    $.doOnCompleted(p success Unit) subscribe()
    import scala.concurrent.duration.DurationInt
    Await.ready(p.future, 1 second)
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

    "all" in {
      val album = Models.mockAlbum()
      val song = Models.mockSong(album = album)
      addSong(song)
      awaitCompletion($.indexAll())
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
      awaitCompletion($.indexAll())
      verifyData(album)
    }
    "no artist duplicates" in {
      val album1 = Models.mockAlbum(title = "a1")
      val album2 = Models.mockAlbum(title = "a2")
      val song1 = Models.mockSong(title = "song1", album = album1)
      val song2 = Models.mockSong(title = "song2", album = album2)
      addSong(song1)
      addSong(song2)
      awaitCompletion($.indexAll())
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
  }

  "incremental (integration)" in {
    // it's a pain in the ass to test for updates using mocks
    val saver = new JsonableSaver
    val $ = fromSaver(saver)
    val album1 = Models.mockAlbum(title = "album1")
    val song1 = Models.mockSong(title = "song1", album = album1, artistName = "artist1")
    $(addSong(song1))
    saver.loadArray[Song] shouldReturn Seq(song1)
    saver.loadArray[Album] shouldReturn Seq(album1)
    saver.loadArray[Artist] shouldReturn Seq(Artist("artist1", Set(album1)))
    val album2 = Models.mockAlbum(title = "album2")
    val song2 = Models.mockSong(title = "song2", album = album2, artistName = "artist1")
    $(addSong(song2))

    saver.loadArray[Song].toSet shouldReturn Set(song1, song2)
    saver.loadArray[Album].toSet shouldReturn Set(album1, album2)
    saver.loadArray[Artist].toSet shouldReturn Set(Artist("artist1", Set(album1, album2)))
    val album3 = Models.mockAlbum(title = "album3")
    val song3 = Models.mockSong(title = "song3", album = album3, artistName = "artist2")
    $(addSong(song3))
    saver.loadArray[Song].toSet shouldReturn Set(song1, song2, song3)
    saver.loadArray[Album].toSet shouldReturn Set(album1, album2, album3)
    saver.loadArray[Artist].toSet shouldReturn Set(Artist("artist1", Set(album1, album2)), Artist("artist2", Set(album3)))
  }

  "quickRefresh" in {
    // setup
    val saver = new JsonableSaver
    val $ = fromSaver(saver)
    val album1 = Models.mockAlbum(title = "album1")
    val song1 = Models.mockSong(title = "song1", album = album1, artistName = "artist1")
    addSong(song1)
    awaitCompletion($.quickRefresh())
    saver.loadArray[Song] shouldReturn Seq(song1)
    saver.loadArray[Album] shouldReturn Seq(album1)
    saver.loadArray[Artist] shouldReturn Seq(Artist("artist1", Set(album1)))

    // song shouldn't be found because it belongs to an album that was already parsed
    val hiddenSong = Models.mockSong(album = album1, artistName = song1.artistName)
    addSong(hiddenSong)
    Thread sleep 1
    val album2 = Models.mockAlbum(title = "album2")
    val song2 = Models.mockSong(title = "song2", album = album2, artistName = "artist1")
    addSong(song2)

    awaitCompletion($.quickRefresh())
    saver.loadArray[Song].toSet shouldReturn Set(song1, song2)
    saver.loadArray[Album].toSet shouldReturn Set(album1, album2)
    saver.loadArray[Artist].toSet shouldReturn Set(Artist("artist1", Set(album1, album2)))
  }
}
