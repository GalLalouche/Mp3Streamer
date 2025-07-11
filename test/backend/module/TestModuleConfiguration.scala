package backend.module

import java.util.UUID

import backend.logging.ScribeUtils
import backend.storage.DbProvider
import com.google.inject.{Guice, Module, Provides}
import com.google.inject.util.Modules
import io.lemonlabs.uri.Url
import models.SongTagParser
import musicfinder.PosterLookup
import net.codingwell.scalaguice.ScalaModule
import slick.jdbc.{H2Profile, JdbcProfile}
import slick.util.AsyncExecutor

import scala.concurrent.ExecutionContext

import common.guice.RichModule.richModule
import common.io.{BaseDirectory, DirectoryRef, MemoryRoot, RootDirectory}
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
    private val _requestToResponseMapper: PartialFunction[WSRequest, FakeWSResponse] =
      PartialFunction.empty,
    private val _root: MemoryRoot = new MemoryRoot,
) {
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

  val module: Module = Modules
    .combine(
      new TestModule,
      new ScalaModule {
        override def configure(): Unit = {
          bind[MemoryRoot].annotatedWith[RootDirectory].toInstance(_root)
          bind[DirectoryRef].annotatedWith[BaseDirectory].toInstance(_root)
          bind[ExecutionContext].toInstance(_ec)
          bind[FakeMusicFinder].toInstance(_mf.opt.getOrElse(new FakeMusicFinder(_root)))
          bind[SongTagParser].to[FakeMusicFinder]
          bind[DbProvider].toInstance(new DbProvider {
            override lazy val profile: JdbcProfile = H2Profile
            override lazy val db: profile.backend.DatabaseDef = {
              val dbId = System.identityHashCode(this) + UUID.randomUUID().toString
              profile.api.Database.forURL(
                url = s"jdbc:h2:mem:test$dbId;DB_CLOSE_DELAY=-1",
                executor = AsyncExecutor.default("Testing", 20),
              )
            }
            override def constraintMangler(name: String) = s"${UUID.randomUUID()}_$name"
          })
          ScribeUtils.noLogs()
        }

        @Provides
        private def provideWSClient: WSClient = new FakeWSClient(getRequest)
      },
    )
    .overrideWith(new ScalaModule {
      @Provides private def posterLookup(@RootDirectory rootDirectory: DirectoryRef): PosterLookup =
        s => rootDirectory.addFile(s.title + ".poster.jpg")
    })

  val injector = Guice.createInjector(module)
}
