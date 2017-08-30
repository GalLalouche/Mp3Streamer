package backend.configs

import backend.Url
import backend.logging.{Logger, StringBuilderLogger}
import common.FakeClock
import common.io.MemoryRoot
import common.rich.RichT._
import play.api.libs.ws._

import scala.concurrent.ExecutionContext

// It's a case class so its copy constructor could be used by clients in order to configure it.
case class TestConfiguration(
    private val _ec: ExecutionContext = new ExecutionContext {
      override def reportFailure(cause: Throwable): Unit = ???
      override def execute(runnable: Runnable): Unit = runnable.run()
    },
    private val _mf: FakeMusicFinder = null,
    private val _urlToBytesMapper: PartialFunction[Url, Array[Byte]] = PartialFunction.empty,
    private val _urlToResponseMapper: PartialFunction[Url, WSResponse] = PartialFunction.empty,
    private val _requestToResponseMapper: PartialFunction[WSRequest, WSResponse] = PartialFunction.empty,
    private val _root: MemoryRoot = new MemoryRoot)
    extends NonPersistentConfig {
  override implicit lazy val db: driver.backend.DatabaseDef = driver.api.Database.forURL(
    s"jdbc:h2:mem:test${System.identityHashCode(this)};DB_CLOSE_DELAY=-1",
    driver = "org.H2.JDBC")
  override implicit val ec: ExecutionContext = _ec
  override implicit val mf: FakeMusicFinder = _mf.opt.getOrElse(new FakeMusicFinder(_root))
  override implicit val logger: Logger = new StringBuilderLogger(new StringBuilder)
  override implicit lazy val rootDirectory: MemoryRoot = _root
  override implicit val clock: FakeClock = new FakeClock

  override def createWsClient() = new WSClient {
    private var wasClosed = false
    // For printing if the client wasn't closed
    private val stackTrace = Thread.currentThread.getStackTrace drop 3

    override def underlying[T] = this.asInstanceOf[T]

    override def url(url: String): WSRequest = {
      if (wasClosed)
        throw new IllegalStateException("WSClient is closed")
      val u = Url(url)
      val partialBuilder: Url => FakeWSRequest =
        if (_urlToBytesMapper isDefinedAt u)
          FakeWSRequest(FakeWSResponse(status = 200, bytes = _urlToBytesMapper(u)))
        else if (_urlToResponseMapper isDefinedAt u)
          FakeWSRequest(_urlToResponseMapper(u))
        else
          FakeWSRequest(_requestToResponseMapper)
      partialBuilder(u)
    }

    override def close() =
      if (wasClosed) throw new IllegalStateException("WSClient was already closed")
      else wasClosed = true

    override def finalize() =
      if (!wasClosed) {
        println("WSClient wasn't closed :( printing stack-trace")
        println(stackTrace mkString "\n")
        println("-----------------------------------------------")
      }
  }
}


