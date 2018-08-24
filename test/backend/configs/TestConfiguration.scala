package backend.configs

import java.util.UUID

import backend.Url
import com.google.inject.Guice
import common.io.MemoryRoot
import common.io.WSAliases._
import common.rich.RichT._

import scala.concurrent.ExecutionContext

// It's a case class so its copy constructor could be used by clients in order to configure it.
case class TestConfiguration(
    private val _ec: ExecutionContext = new ExecutionContext {
      override def reportFailure(cause: Throwable): Unit = ???
      override def execute(runnable: Runnable): Unit = runnable.run()
    },
    private val _mf: FakeMusicFinder = null,
    private val _urlToBytesMapper: PartialFunction[Url, Array[Byte]] = PartialFunction.empty,
    private val _urlToResponseMapper: PartialFunction[Url, FakeWSResponse] = PartialFunction.empty,
    private val _requestToResponseMapper: PartialFunction[WSRequest, FakeWSResponse] = PartialFunction.empty,
    private val _root: MemoryRoot = new MemoryRoot)
    extends NonPersistentConfig {
  override protected val ec: ExecutionContext = _ec
  override lazy val db: profile.backend.DatabaseDef = {
    profile.api.Database.forURL(
      s"jdbc:h2:mem:test${System.identityHashCode(this) + UUID.randomUUID().toString};DB_CLOSE_DELAY=-1")
  }
  override val mf: FakeMusicFinder = _mf.opt.getOrElse(new FakeMusicFinder(_root))
  override lazy val rootDirectory: MemoryRoot = _root

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

  override def createWsClient(): WSClient = new FakeWSClient(getRequest)
  override val module = new TestModule
  override val injector = Guice createInjector module
}


