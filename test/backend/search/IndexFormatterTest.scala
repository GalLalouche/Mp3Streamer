package backend.search

import backend.module.TestModuleConfiguration
import backend.search.MetadataCacher.CacheUpdate
import controllers.websockets.WebSocketRef
import controllers.websockets.WebSocketRef.AsyncWebSocketRefReader
import net.codingwell.scalaguice.InjectorExtensions._
import org.mockito.Mockito.when
import org.scalatest.{Assertion, AsyncFreeSpec, OneInstancePerTest}
import org.scalatest.mockito.MockitoSugar._
import rx.lang.scala.Observable

import scala.concurrent.{ExecutionContext, Future}

import scalaz.syntax.functor.ToFunctorOps

import common.MockitoSyrup
import common.io.MemoryRoot
import common.test.AuxSpecs

class IndexFormatterTest extends AsyncFreeSpec with AuxSpecs with OneInstancePerTest {
  private val indexer = mock[Indexer]
  private val $ = new IndexFormatter(indexer)

  private val injector = TestModuleConfiguration().injector
  private val root = injector.instance[MemoryRoot]
  private val cacheResult =
    Observable.just(CacheUpdate(1, 2, root.addSubDir("foo")), CacheUpdate(2, 2, root.addSubDir("bar")))

  private implicit val ec: ExecutionContext = injector.instance[ExecutionContext]

  private def verifyCacheUpdates(r: AsyncWebSocketRefReader): Future[Assertion] = {
    val websocket = mock[WebSocketRef]
    r.run(websocket) >| MockitoSyrup.captureAll(websocket)[String](_ broadcast _) shouldReturn Vector(
      """{"finished":1,"total":2,"currentDir":"foo"}""",
      """{"finished":2,"total":2,"currentDir":"bar"}""",
      "Finished",
    )
  }

  "cacheAll" in {
    when(indexer.cacheAll()).thenReturn(cacheResult)
    verifyCacheUpdates($.cacheAll())
  }

  "quickRefresh" in {
    when(indexer.quickRefresh()).thenReturn(cacheResult)
    verifyCacheUpdates($.quickRefresh())
  }
}
