package backend.search

import backend.module.TestModuleConfiguration
import backend.search.cache.{CacheUpdate, MetadataCacher}
import net.codingwell.scalaguice.InjectorExtensions._
import org.mockito.Mockito.{verify, when}
import org.scalatest.{Assertion, AsyncFreeSpec, OneInstancePerTest, Succeeded}
import org.scalatest.mockito.MockitoSugar._
import rx.lang.scala.Observable
import rx.lang.scala.subjects.ReplaySubject
import songs.SongSelectorState

import scala.concurrent.Future

import scalaz.syntax.bind.ToBindOps
import common.rich.func.BetterFutureInstances._

import common.io.{DirectoryRef, MemoryRoot}
import common.rich.RichObservable._
import common.test.AsyncAuxSpecs

class IndexerTest extends AsyncFreeSpec with AsyncAuxSpecs with OneInstancePerTest {
  private val searchState = mock[SearchState]
  when(searchState.update()).thenReturn(Future.successful(()))
  private val songSelectorState = mock[SongSelectorState]
  when(songSelectorState.update()).thenReturn(Future.successful(()))
  private val metadataCacher = mock[MetadataCacher]
  private val subject = ReplaySubject[DirectoryRef]()

  private val $ = new Indexer(searchState, songSelectorState, metadataCacher, subject)

  private val injector = TestModuleConfiguration().injector
  private val root = injector.instance[MemoryRoot]
  private val dir1 = root.addSubDir("foo")
  private val update1 = CacheUpdate(1, 2, dir1)
  private val dir2 = root.addSubDir("bar")
  private val update2 = CacheUpdate(2, 2, dir2)
  private val cacheResult = Observable.just(update1, update2)

  private def verifyCacheUpdates(observable: Observable[CacheUpdate]): Future[Assertion] =
    observable.toFuture[Vector].map {e =>
      e shouldReturn Vector(update1, update2)
      verify(searchState).update()
      verify(songSelectorState).update()
      Succeeded
    }

  "cacheAll" in {
    when(metadataCacher.cacheAll()).thenReturn(cacheResult)
    verifyCacheUpdates($.cacheAll())
  }

  "quickRefresh" in {
    when(metadataCacher.quickRefresh()).thenReturn(cacheResult)
    verifyCacheUpdates($.quickRefresh()) >>
        (subject.take(2).toFuture[Vector] shouldEventuallyReturn Vector(dir1, dir2))
  }
}
