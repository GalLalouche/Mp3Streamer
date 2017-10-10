package backend.configs

import backend.Url
import backend.logging.{Logger, StringBuilderLogger}
import common.FakeClock
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
  override implicit lazy val db: profile.backend.DatabaseDef = profile.api.Database.forURL(
    s"jdbc:h2:mem:test${System.identityHashCode(this)};DB_CLOSE_DELAY=-1", profile.getClass.getSimpleName)
  override implicit val ec: ExecutionContext = _ec
  override implicit val mf: FakeMusicFinder = _mf.opt.getOrElse(new FakeMusicFinder(_root))
  override implicit val logger: Logger = new StringBuilderLogger(new StringBuilder)
  override implicit lazy val rootDirectory: MemoryRoot = _root
  override implicit val clock: FakeClock = new FakeClock

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
}


