package backend.configs

import java.net.HttpURLConnection

import backend.Url
import backend.logging.{Logger, StringBuilderLogger}
import common.io.{MemoryDir, MemoryRoot}
import models.MusicFinder
import org.jsoup.nodes.Document

import scala.concurrent.{ExecutionContext, Future}

// It's a case class so its copy constructor could be used by clients in order to configure it.
case class TestConfiguration(private val _ec: ExecutionContext = new ExecutionContext {
                               override def reportFailure(cause: Throwable): Unit = ???
                               override def execute(runnable: Runnable): Unit = runnable.run()
                             },
                             private val _mf: MusicFinder {type D = MemoryDir} = null,
                             private val _documentDownloader: Url => Document = e => ???,
                             private val _httpTransformer: HttpURLConnection => HttpURLConnection = e => ???,
                             private val _root: MemoryRoot = new MemoryRoot)
    extends NonPersistentConfig {
  override final type D = MemoryDir
  override implicit val ec: ExecutionContext = _ec
  override implicit val mf = _mf
  override def downloadDocument(url: Url): Future[Document] = Future successful _documentDownloader(url)
  override def connect(http: HttpURLConnection): Future[HttpURLConnection] = Future successful _httpTransformer(http)
  override implicit val logger: Logger = new StringBuilderLogger(TestConfiguration.loggingHistory)
}

object TestConfiguration {
  val loggingHistory = new StringBuilder
}
