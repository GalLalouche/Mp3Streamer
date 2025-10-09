package server

import java.util.concurrent.atomic.AtomicInteger

import backend.module.TestModuleConfiguration
import com.google.inject.{Guice, Injector, Module}
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import net.codingwell.scalaguice.ScalaModule
import org.scalatest.{Assertion, AsyncFreeSpec, BeforeAndAfterAll, Succeeded}
import org.scalatest.tags.Slow
import play.api.libs.json.{JsArray, JsObject, JsValue}
import sttp.client3
import sttp.client3.{asByteArray, HttpClientFutureBackend, ResolveRelativeUrisBackend, Response}
import sttp.client3.playJson._
import sttp.model.Uri

import scala.concurrent.Future

import common.rich.func.BetterFutureInstances._
import common.rich.func.ToMoreApplicativeOps.toLazyApplicativeUnitOps

import common.concurrency.DaemonExecutionContext
import common.guice.RichModule.richModule
import common.rich.RichFuture.richFuture
import common.rich.primitives.RichEither._
import common.test.AsyncAuxSpecs

/**
 * A test that is not coupled with any specific server implementation, e.g., http4s vs Play.
 * Instead, it initializes an HTTP server and just makes plain old HTTP requests to it. Mind you,
 * this not actually a proper "end-to-end" test, since, for example, it will write to disk.
 *
 * @serverModule
 *   Should contain a binding for [[Server]].
 */
@Slow
private abstract class HttpServerSpecs(serverModule: Module)
    extends AsyncFreeSpec
    with AsyncAuxSpecs
    with BeforeAndAfterAll {
  // Apparently, trying to load multiple test suites in HttpTestSuite would only work properly if
  // using the default execution context. Since that one is single threaded, and we don't want to
  // be, we have to resort to a few hacks:
  // 1. beforeAll and afterAll are blocking, instead of using the async version (it gets deadlocked
  //    otherwise).
  // 2. The execution test passed via guice is different from the one provided by the async test
  //    framework.
  private var runningServer: RunningServer = _
  protected override def beforeAll() = runningServer = injector.instance[Server].start(port).get
  protected override def afterAll() = runningServer.stop().whenMLazy(runningServer != null)
  protected def baseTestModule: TestModuleConfiguration =
    TestModuleConfiguration(_ec = DaemonExecutionContext("HttpServerSpecs", n = 20))
  /** This module should contain an implementation of [[Server]] */
  protected def overridingModule: Module = new ScalaModule {}
  protected final lazy val injector: Injector = Guice.createInjector(
    serverModule.overrideWith(baseTestModule.module).overrideWith(overridingModule),
  )

  private val port = HttpServerSpecs.port.getAndIncrement()
  private def backend = ResolveRelativeUrisBackend(
    HttpClientFutureBackend(),
    Uri(scheme = "http", host = "localhost", port = port),
  )
  private def request = client3.basicRequest.followRedirects(false)

  def getBytes(u: Uri): Future[Array[Byte]] =
    backend.send(request.get(u).response(asByteArray)).map(_.body.getOrThrow.array)
  def getString(u: Uri): Future[String] =
    backend.send(request.get(u)).map(_.body.getOrThrow)
  def getRaw(u: Uri): Future[Response[_]] = backend.send(request.get(u))
  def getJson(u: Uri): Future[JsValue] =
    backend.send(request.get(u).response(asJson[JsValue])).map(_.body.getOrThrow)

  def putJson(u: Uri, json: JsValue): Future[String] =
    backend.send(request.put(u).body(json)).map(_.body.getOrThrow)

  def deleteString(u: Uri): Future[String] =
    backend.send(request.delete(u)).map(_.body.getOrThrow)

  // TODO Maybe use https://github.com/skyscreamer/JSONassert?
  implicit class jsValueSpecs(private val $ : JsValue) {
    def shouldContain(other: JsValue): Assertion = {
      ($, other) match {
        case (o1: JsObject, o2: JsObject) =>
          o1.value.keys shouldContainAllOf (o2.keys)
          for (k <- o2.keys)
            o1.value(k) shouldContain (o2.value(k))
        case (a1: JsArray, a2: JsArray) =>
          for (i <- a2.value.indices)
            a1.value(i) shouldContain (a2.value(i))
        case (j1, j2) => j1 shouldReturn j2
      }
      Succeeded
    }
  }
}

private object HttpServerSpecs {
  private val port = new AtomicInteger(4242)
}
