package backend.search

import java.util.concurrent.{LinkedBlockingQueue, TimeUnit}

import backend.module.{FakeMusicFinder, TestModuleConfiguration}
import backend.search.MetadataCacher.CacheUpdate
import models._
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.{FreeSpec, OneInstancePerTest}
import org.scalatest.matchers.{Matcher, MatchResult}
import rx.lang.scala.Observable

import scala.concurrent.{Await, ExecutionContext, Promise}

import common.rich.RichObservable._
import common.AuxSpecs
import common.io.{JsonableSaver, MemoryRoot}
import common.json.Jsonable
import common.rich.RichFuture._

class MetadataCacherTest extends FreeSpec with OneInstancePerTest with AuxSpecs {
  private val fakeModelFactory = new FakeModelFactory
  private val root: MemoryRoot = new MemoryRoot
  private val songs = root.addSubDir("songs")
  private val mf: FakeMusicFinder = new FakeMusicFinder(songs)
  private val c = TestModuleConfiguration().copy(_root = root, _mf = mf)
  private implicit val ec: ExecutionContext = c.injector.instance[ExecutionContext]
  private val jsonableSaver = c.injector.instance[JsonableSaver]
  private val albumFactory = c.injector.instance[AlbumFactory]
  private val fakeJsonable = new FakeModelJsonable

  import albumFactory.AlbumFactorySongOps
  import fakeJsonable.FakeJsonable

  // TODO replace this hack with a less hacky hack
  // This hack enforces an up-cast of memory models subtypes (e.g., MemorySong <: Song) to their base model.
  // This is needed since SearchIndexer saves the data under Song, but because FakeModelFactory returns a
  // MemorySong, the verifier will try to load a MemorySong, resulting in no data, since JsonableSaver loads
  // data using a compile-time manifest. Yeah... :|
  private def verifyData[T: Jsonable : Manifest](xs: Seq[T]): Unit =
    jsonableSaver.loadArray[T] shouldMultiSetEqual xs
  private def verifyData(data: Song*): Unit = verifyData(data)
  private def verifyData(data: Album*)(implicit d: DummyImplicit): Unit = verifyData(data)
  private def verifyData(data: Artist*)(implicit d: DummyImplicit, d2: DummyImplicit): Unit = verifyData(data)

  private val $ = new MetadataCacher(jsonableSaver, ec, mf, albumFactory)

  private def awaitCompletion($: Observable[Any]): Unit = {
    val p = Promise[Unit]
    $.doOnCompleted(p success Unit) subscribe()
    import scala.concurrent.duration.DurationInt
    Await.ready(p.future, 2.seconds)
  }
  private def indexAllAndWait(): Unit = awaitCompletion($.cacheAll())
  private def quickRefreshAndWait(): Unit = awaitCompletion($.quickRefresh())

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
      val song = mf.copySong(fakeModelFactory.song(title = "song1", albumName = "album"))
      mf.copySong(fakeModelFactory.song(title = "song2", albumName = "album"))
      indexAllAndWait()
      verifyData(song.album)
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
      val q = new LinkedBlockingQueue[CacheUpdate]()
      $.cacheAll().foreach(q.put)
      def matchWithIndices(current: Int, total: Int) = new Matcher[CacheUpdate] {
        override def apply(left: CacheUpdate): MatchResult = {
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

  "quickRefresh" - {
    "only updates new directories" in {
      // setup
      val song1 = mf.copySong(fakeModelFactory.song(title = "song1", albumName = "album1", artistName = "artist1"))
      val album1 = song1.album

      indexAllAndWait()

      verifyData(song1)
      verifyData(album1)
      verifyData(Artist("artist1", Set(album1)))

      // song2 shouldn't be found because it belongs to an album that was already parsed.
      mf.copySong(fakeModelFactory.song(albumName = album1.title, artistName = song1.artistName))
      Thread sleep 1
      val song2 = mf.copySong(fakeModelFactory.song(title = "song2", albumName = "album2", artistName = "artist1"))
      val album2 = song2.album

      quickRefreshAndWait()

      verifyData(song1, song2)
      verifyData(album1, album2)
      verifyData(Artist("artist1", Set(album1, album2)))
    }
    "throws if no index exists" in {
      an[IllegalStateException] shouldBe thrownBy {$.quickRefresh().toFuture[Vector].get}
    }
  }

  "incremental (integration)" in {
    val song1 = mf.copySong(fakeModelFactory.song(title = "song1", albumName = "album1", artistName = "artist1"))
    val album1 = song1.album
    indexAllAndWait()
    verifyData(song1)
    verifyData(album1)
    verifyData(Artist("artist1", Set(album1)))

    val song2 = mf.copySong(fakeModelFactory.song(title = "song2", albumName = "album2", artistName = "artist1"))
    val album2 = song2.album
    quickRefreshAndWait()
    verifyData(song1, song2)
    verifyData(album1, album2)
    verifyData(Artist("artist1", Set(album1, album2)))

    val song3 = mf.copySong(fakeModelFactory.song(title = "song3", albumName = "album3", artistName = "artist2"))
    val album3 = song3.album
    quickRefreshAndWait()
    verifyData(song1, song2, song3)
    verifyData(album1, album2, album3)
    verifyData(Artist("artist1", Set(album1, album2)), Artist("artist2", Set(album3)))
  }
}
