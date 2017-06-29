package backend.configs

import java.net.HttpURLConnection

import backend.Url
import backend.logging.{Logger, StringBuilderLogger}
import common.FakeClock
import common.io.MemoryRoot
import org.jsoup.nodes.Document

import scala.concurrent.{ExecutionContext, Future}

// It's a case class so its copy constructor could be used by clients in order to configure it.
case class TestConfiguration(private val _ec: ExecutionContext = new ExecutionContext {
                               override def reportFailure(cause: Throwable): Unit = ???
                               override def execute(runnable: Runnable): Unit = runnable.run()
                             },
                             private val _mf: FakeMusicFinder = null,
                             private val _documentDownloader: Url => Document = _ => ???,
                             private val _httpTransformer: HttpURLConnection => HttpURLConnection = _ => ???,
                             private val _root: MemoryRoot = new MemoryRoot)
    extends NonPersistentConfig {
  override implicit val ec: ExecutionContext = _ec
  override implicit val mf: FakeMusicFinder = _mf
  override def downloadDocument(url: Url): Future[Document] = Future successful _documentDownloader(url)
  override def connect(url: Url, config: (HttpURLConnection) => Unit) = Future successful {
    val $ = new HttpURLConnection(url.toURL) {
      override def disconnect() = ???
      override def usingProxy() = ???
      override def connect() = ???
    }
    config($)
    _httpTransformer($)
  }
  override implicit val logger: Logger = new StringBuilderLogger(TestConfiguration.loggingHistory)
  override implicit lazy val rootDirectory: MemoryRoot = _root
  override implicit val clock: FakeClock = new FakeClock
}

object TestConfiguration {
  val loggingHistory = new StringBuilder
}
