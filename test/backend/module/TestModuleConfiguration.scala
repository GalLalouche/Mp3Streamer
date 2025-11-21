package backend.module

import com.google.inject.{Guice, Injector, Module, Provides}
import com.google.inject.util.Modules
import io.lemonlabs.uri.Url
import net.codingwell.scalaguice.ScalaModule

import scala.concurrent.ExecutionContext

import common.io.{BaseDirectory, DirectoryRef, MemoryRoot}
import common.io.WSAliases._
import common.rich.RichT._

// This is a case class so its copy constructor could be used by clients in order to configure it.
case class TestModuleConfiguration(
    private val _ec: ExecutionContext = new ExecutionContext {
      override def reportFailure(cause: Throwable): Unit = throw cause
      override def execute(runnable: Runnable): Unit = runnable.run()
    },
    private val _mf: FakeMusicFinder = null,
    private val _root: MemoryRoot = new MemoryRoot,
    private val _urlToBytesMapper: PartialFunction[Url, Array[Byte]] = PartialFunction.empty,
    private val _urlToResponseMapper: PartialFunction[Url, FakeWSResponse] = PartialFunction.empty,
    private val _requestToResponseMapper: PartialFunction[WSRequest, FakeWSResponse] =
      PartialFunction.empty,
) {
  lazy val module: Module = Modules.combine(
    TestModule,
    new ScalaModule {
      override def configure(): Unit = {
        bind[MemoryRoot].toInstance(_root)
        bind[DirectoryRef].annotatedWith[BaseDirectory].toInstance(_root)
        bind[ExecutionContext].toInstance(_ec)
        bind[FakeMusicFinder].toInstance(_mf.opt.getOrElse(new FakeMusicFinder(_root)))
      }

      @Provides
      private def provideWSClient: WSClient = new FakeWSClient(getRequest)
    },
  )

  lazy val injector: Injector = Guice.createInjector(module)

  private def getRequest(u: Url): WSRequest = {
    val partialRequest: Url => WSRequest =
      if (_urlToBytesMapper.isDefinedAt(u))
        FakeWSRequest(FakeWSResponse(status = 200, bytes = _urlToBytesMapper(u)))
      else if (_urlToResponseMapper.isDefinedAt(u))
        FakeWSRequest(_urlToResponseMapper(u))
      else
        FakeWSRequest(_requestToResponseMapper)
    partialRequest(u)
  }
}
