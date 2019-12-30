package backend.module

import java.util.UUID

import backend.Url
import backend.storage.DbProvider
import com.google.inject.{Guice, Module, Provides}
import com.google.inject.util.Modules
import net.codingwell.scalaguice.ScalaModule
import slick.jdbc.{H2Profile, JdbcProfile}

import scala.concurrent.ExecutionContext

import common.io.{InternetTalker, MemoryRoot, RootDirectory}
import common.io.WSAliases._
import common.rich.RichT._

// It's a case class so its copy constructor could be used by clients in order to configure it.
case class TestModuleConfiguration(
    private val _ec: ExecutionContext = new ExecutionContext {
      override def reportFailure(cause: Throwable): Unit = ???
      override def execute(runnable: Runnable): Unit = runnable.run()
    },
    private val _mf: FakeMusicFinder = null,
    private val _urlToBytesMapper: PartialFunction[Url, Array[Byte]] = PartialFunction.empty,
    private val _urlToResponseMapper: PartialFunction[Url, FakeWSResponse] = PartialFunction.empty,
    private val _requestToResponseMapper: PartialFunction[WSRequest, FakeWSResponse] = PartialFunction.empty,
    private val _root: MemoryRoot = new MemoryRoot,
) {

  private def getRequest(u: Url): WSRequest = {
    val partialRequest: Url => WSRequest =
      if (_urlToBytesMapper isDefinedAt u)
        FakeWSRequest(FakeWSResponse(status = 200, bytes = _urlToBytesMapper(u)))
      else if (_urlToResponseMapper isDefinedAt u)
        FakeWSRequest(_urlToResponseMapper(u))
      else
        FakeWSRequest(_requestToResponseMapper)
    partialRequest(u)
  }

  val module: Module = Modules.combine(new TestModule, new ScalaModule {
    override def configure(): Unit = {
      bind[MemoryRoot].annotatedWith[RootDirectory] toInstance _root
      bind[ExecutionContext] toInstance _ec
      bind[FakeMusicFinder] toInstance _mf.opt.getOrElse(new FakeMusicFinder(_root))
      bind[DbProvider] toInstance new DbProvider {
        override lazy val profile: JdbcProfile = H2Profile
        override lazy val db: profile.backend.DatabaseDef = {
          profile.api.Database.forURL(
            s"jdbc:h2:mem:test${System.identityHashCode(this) + UUID.randomUUID().toString};DB_CLOSE_DELAY=-1")
        }
      }
    }

    @Provides
    private def provideInternetTalker(_ec: ExecutionContext): InternetTalker = new InternetTalker {
      override def execute(runnable: Runnable) = _ec.execute(runnable)
      override def reportFailure(cause: Throwable) = _ec.reportFailure(cause)
      override protected def createWsClient(): WSClient = new FakeWSClient(getRequest)
    }
  })

  val injector = Guice.createInjector(module)
}
