package search

import java.util.concurrent.{LinkedBlockingQueue, TimeUnit}

import backend.configs.{FakeMusicFinder, TestConfiguration}
import common.io.{JsonableSaver, MemoryRoot}
import common.rich.RichFuture._
import common.{AuxSpecs, Jsonable}
import models.Artist
import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatest.{FreeSpec, OneInstancePerTest}
import rx.lang.scala.Observable
import search.MetadataCacher.IndexUpdate
import search.ModelsJsonable._

import scala.concurrent.{Await, Promise}

class MetadataCacherTest extends FreeSpec with OneInstancePerTest with AuxSpecs {
  private implicit val root = new MemoryRoot
  private val songs = root.addSubDir("songs")
  private val jsonableSaver = new JsonableSaver
  private def verifyData[T: Jsonable : Manifest](xs: T*) {
    jsonableSaver.loadArray[T].toSet shouldReturn xs.toSet
  }

  private val mf = new FakeMusicFinder(songs)
  private implicit val c = TestConfiguration().copy(_root = root, _mf = mf)
  private val $ = new MetadataCacher(jsonableSaver)(c)

  private def awaitCompletion($: Observable[Any]) = {
    val p = Promise[Unit]
    $.doOnCompleted(p success Unit) subscribe()
    import scala.concurrent.duration.DurationInt
    Await.ready(p.future, 1 second)
  }
  private def indexAllAndWait() = {
    awaitCompletion($.indexAll())
  }

  "index" - {
    "all" in {
      val album = Models.mockAlbum()
      val song = Models.mockSong(album = album)
      mf.addSong(song)
      indexAllAndWait()
      verifyData(song)
      verifyData(album)
      verifyData(Artist(album.artistName, Set(album)))
    }
    val album = Models.mockAlbum()
    "no album duplicates" in {
      val song1 = Models.mockSong(title = "song1", album = album)
      val song2 = Models.mockSong(title = "song2", album = album)
      mf.addSong(song1)
      mf.addSong(song2)
      indexAllAndWait()
      verifyData(album)
    }
    "no artist duplicates" in {
      val album1 = Models.mockAlbum(title = "a1")
      val album2 = Models.mockAlbum(title = "a2")
      val song1 = Models.mockSong(title = "song1", album = album1)
      val song2 = Models.mockSong(title = "song2", album = album2)
      mf.addSong(song1)
      mf.addSong(song2)
      indexAllAndWait()
      verifyData(Artist(song1.artistName, Set(album1, album2)))
    }
    "with sink" in {
      val album1 = Models.mockAlbum(title = "a1")
      val album2 = Models.mockAlbum(title = "a2")
      val song1 = Models.mockSong(title = "song1", album = album1)
      val song2 = Models.mockSong(title = "song2", album = album2)
      mf.addSong(song1)
      mf.addSong(song2)
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
    val album1 = Models.mockAlbum(title = "album1")
    val song1 = Models.mockSong(title = "song1", album = album1, artistName = "artist1")
    $.!(mf.addSong(song1)).get
    verifyData(song1)
    verifyData(album1)
    verifyData(Artist("artist1", Set(album1)))
    val album2 = Models.mockAlbum(title = "album2")
    val song2 = Models.mockSong(title = "song2", album = album2, artistName = "artist1")
    $.!(mf.addSong(song2)).get

    verifyData(song1, song2)
    verifyData(album1, album2)
    verifyData(Artist("artist1", Set(album1, album2)))
    val album3 = Models.mockAlbum(title = "album3")
    val song3 = Models.mockSong(title = "song3", album = album3, artistName = "artist2")
    $.!(mf.addSong(song3)).get
    verifyData(song1, song2, song3)
    verifyData(album1, album2, album3)
    verifyData(Artist("artist1", Set(album1, album2)), Artist("artist2", Set(album3)))
  }

  "quickRefresh" in {
    def quickRefreshAndWait() = awaitCompletion($.quickRefresh())
    // setup
    val album1 = Models.mockAlbum(title = "album1")
    val song1 = Models.mockSong(title = "song1", album = album1, artistName = "artist1")
    mf.addSong(song1)

    quickRefreshAndWait()

    verifyData(song1)
    verifyData(album1)
    verifyData(Artist("artist1", Set(album1)))

    // song shouldn't be found because it belongs to an album that was already parsed
    val hiddenSong = Models.mockSong(album = album1, artistName = song1.artistName)
    mf.addSong(hiddenSong)
    Thread sleep 1
    val album2 = Models.mockAlbum(title = "album2")
    val song2 = Models.mockSong(title = "song2", album = album2, artistName = "artist1")
    mf.addSong(song2)

    quickRefreshAndWait()

    verifyData(song1, song2)
    verifyData(album1, album2)
    verifyData(Artist("artist1", Set(album1, album2)))
  }
}
