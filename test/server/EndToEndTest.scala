package server

import java.util.concurrent.atomic.AtomicInteger

import backend.module.TestModuleConfiguration
import com.google.inject.{Guice, Injector, Module}
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import net.codingwell.scalaguice.ScalaModule
import org.scalatest.AsyncFreeSpec
import play.api.libs.json.JsValue
import sttp.client3
import sttp.client3.{asByteArray, HttpClientFutureBackend, ResolveRelativeUrisBackend, Response}
import sttp.client3.playJson._
import sttp.model.Uri

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.BetterFutureInstances._
import common.rich.func.ToMoreApplicativeOps.toLazyApplicativeUnitOps
import common.rich.func.ToMoreFunctorOps.toMoreFunctorOps

import common.concurrency.DaemonExecutionContext
import common.guice.RichModule.richModule
import common.rich.primitives.RichEither._
import common.test.{AsyncAuxSpecs, BeforeAndAfterAllAsync}

/**
 * A test that is not coupled with any specific server implementation, e.g., http4s vs Play.
 * Instead, it initializes an HTTP server and just makes plain old HTTP requests to it. Mind you,
 * this not actually a proper "end-to-end" test, since, for example, it will write to disk.
 */
private abstract class EndToEndTest
    extends AsyncFreeSpec
    with AsyncAuxSpecs
    with BeforeAndAfterAllAsync {
  implicit override def executionContext: ExecutionContext =
    DaemonExecutionContext("EndToEndTest", 20)
  protected def baseTestModule: TestModuleConfiguration =
    TestModuleConfiguration(_ec = executionContext)
  /** This module should contain an implementation of [[Server]] */
  protected def serverModule: Module
  protected def overridingModule: Module = new ScalaModule {}
  protected final lazy val injector: Injector = Guice.createInjector(
    serverModule.overrideWith(baseTestModule.module).overrideWith(overridingModule),
  )
  private var runningServer: RunningServer = _

  private val port = EndToEndTest.port.getAndIncrement()
  protected override def beforeAll() =
    injector.instance[Server].start(port).listen(runningServer = _)
  protected override def afterAll() = runningServer.stop().whenMLazy(runningServer != null)
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
}

private object EndToEndTest {
  private val port = new AtomicInteger(4242)
}
