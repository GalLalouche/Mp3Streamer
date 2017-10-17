package backend.search

import java.util.concurrent.{LinkedBlockingQueue, TimeUnit}

import backend.configs.{FakeMusicFinder, TestConfiguration}
import backend.search.MetadataCacher.IndexUpdate
import common.io.{FormatSaver, MemoryRoot}
import common.rich.RichFuture._
import common.{AuxSpecs, Jsonable}
import models.{Album, Artist, FakeModelFactory, FakeModelJsonable, Song}
import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatest.{FreeSpec, OneInstancePerTest}
import rx.lang.scala.Observable

import scala.concurrent.{Await, Promise}

class MetadataCacherTest extends FreeSpec with OneInstancePerTest with AuxSpecs {
  private val fakeModelFactory = new FakeModelFactory
  private implicit val root = new MemoryRoot
  private val songs = root.addSubDir("songs")
  private val jsonableSaver = new FormatSaver
  private val fakeJsonable = new FakeModelJsonable
  import fakeJsonable._

  // TODO replace this hack with a less hacky hack
  // This hack enforces an up-cast of memory models subtypes (e.g., MemorySong <: Song) to their
  // base model. This is needed since MetadataCacher saves the data under Song, but because
  // FakeModelFactory returns a MemorySong, the verifier will try to load a MemorySong, resulting in
  // no data, since JsonableSaver loads data using a compile-time manifest. Yeah... :|
  private def verifyData[T: Jsonable : Manifest](xs: Seq[T]) {
    jsonableSaver.loadArray[T].toSet shouldReturn xs.toSet
  }
  def verifyData(data: Song*): Unit = verifyData(data)
  // The increasing number of dummy implicits is needed because of type erasure.
  def verifyData(data: Album*)(implicit d: DummyImplicit): Unit = verifyData(data)
  def verifyData(data: Artist*)(implicit d1: DummyImplicit, d2: DummyImplicit): Unit = verifyData(data)

  private val mf = new FakeMusicFinder(songs)
  private implicit val c = TestConfiguration().copy(_root = root, _mf = mf)
  private val $ = new MetadataCacher(jsonableSaver)

  private def awaitCompletion($: Observable[Any]) = {
    val p = Promise[Unit]
    $.doOnCompleted(p success Unit) subscribe()
    import scala.concurrent.duration.DurationInt
    Await.ready(p.future, 2 second)
  }
  private def indexAllAndWait() = {
    awaitCompletion($.indexAll())
  }

  "index" - {
    "all" in {
      val song = mf.copySong(fakeModelFactory.song())
      val album = song.album
      indexAllAndWait()
      verifyData(song)
      verifyData(album)
      verifyData(Artist(album.artistName, Set(album)))
    }
    "no album duplicates" in {
      val song1 = mf.copySong(fakeModelFactory.song(title = "song1", albumName = "album"))
      mf.copySong(fakeModelFactory.song(title = "song2", albumName = "album"))
      indexAllAndWait()
      verifyData(song1.album)
    }
    "no artist duplicates" in {
      val song1 = mf.copySong(fakeModelFactory.song(title = "song1", albumName = "a1"))
      val song2 = mf.copySong(fakeModelFactory.song(title = "song2", albumName = "a2"))
      indexAllAndWait()
      verifyData(Artist(song1.artistName, Set(song1.album, song2.album)))
    }
    "with sink" in {
      mf.copySong(fakeModelFactory.song(title = "song1", albumName = "a1"))
      mf.copySong(fakeModelFactory.song(title = "song2", albumName = "a2"))
      val q = new LinkedBlockingQueue[IndexUpdate]()
      $.indexAll().foreach(q.put)
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
    val song1 = mf.copySong(fakeModelFactory.song(title = "song1", albumName = "album1", artistName = "artist1"))
    val album1 = song1.album
    $.processDirectory(album1.dir).get
    verifyData(song1)
    verifyData(album1)
    verifyData(Artist("artist1", Set(album1)))

    val song2 = mf.copySong(fakeModelFactory.song(title = "song2", albumName = "album2", artistName = "artist1"))
    val album2 = song2.album
    $.processDirectory(album2.dir).get
    verifyData(song1, song2)
    verifyData(album1, album2)
    verifyData(Artist("artist1", Set(album1, album2)))

    val song3 = mf.copySong(fakeModelFactory.song(title = "song3", albumName = "album3", artistName = "artist2"))
    val album3 = song3.album
    $.processDirectory(album3.dir).get
    verifyData(song1, song2, song3)
    verifyData(album1, album2, album3)
    verifyData(Artist("artist1", Set(album1, album2)), Artist("artist2", Set(album3)))
  }

  "quickRefresh" in {
    def quickRefreshAndWait() = awaitCompletion($.quickRefresh())
    // setup
    val song1 = mf.copySong(fakeModelFactory.song(title = "song1", albumName = "album1", artistName = "artist1"))
    val album1 = song1.album

    quickRefreshAndWait()

    verifyData(song1)
    verifyData(album1)
    verifyData(Artist("artist1", Set(album1)))

    // song shouldn't be found because it belongs to an album that was already parsed
    mf.copySong(fakeModelFactory.song(albumName = album1.title, artistName = song1.artistName))
    Thread sleep 1
    val song2 = mf.copySong(fakeModelFactory.song(title = "song2", albumName = "album2", artistName = "artist1"))
    val album2 = song2.album

    quickRefreshAndWait()

    verifyData(song1, song2)
    verifyData(album1, album2)
    verifyData(Artist("artist1", Set(album1, album2)))
  }
}
