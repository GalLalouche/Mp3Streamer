package backend.configs

import java.net.HttpURLConnection

import backend.Url
import common.io.MemoryRoot
import models.MusicFinder
import org.jsoup.nodes.Document

import scala.concurrent.{ExecutionContext, Future}

// It's a case class so its copy constructor could be used by clients in order to configure it.
case class TestConfiguration(private val _ec: ExecutionContext = new ExecutionContext {
                               override def reportFailure(cause: Throwable): Unit = ???
                               override def execute(runnable: Runnable): Unit = runnable.run()
                             },
                             private val _mf: MusicFinder = null,
                             private val _documentDownloader: Url => Document = e => ???,
                             private val _httpTransformer: HttpURLConnection => HttpURLConnection = e => ???,
                             private val _root: MemoryRoot = new MemoryRoot)
    extends NonPersistentConfig {
  override implicit val ec: ExecutionContext = _ec
  override implicit val mf: MusicFinder = _mf
  override def downloadDocument(url: Url): Future[Document] = Future successful _documentDownloader(url)
  override def connect(http: HttpURLConnection): Future[HttpURLConnection] = Future successful _httpTransformer(http)
}
