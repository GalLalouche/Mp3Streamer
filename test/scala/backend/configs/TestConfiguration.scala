package backend.configs

import java.net.HttpURLConnection

import backend.Url
import models.MusicFinder
import org.jsoup.nodes.Document

import scala.concurrent.{ExecutionContext, Future}

/** No IO */
class TestConfigurationInstance extends NonPersistentConfig {
  override implicit val ec: ExecutionContext = new ExecutionContext {
    override def reportFailure(cause: Throwable): Unit = ???
    override def execute(runnable: Runnable): Unit = runnable.run()
  }

  override implicit val mf: MusicFinder = null
  override def downloadDocument(url: Url): Future[Document] =
    throw new NotImplementedError("No default implementation for downloadDocument")
  override def connect(http: HttpURLConnection): Future[HttpURLConnection] =
    throw new NotImplementedError("No default implementation for httpUrlConnection")

  def withDocumentDownloader(dd: Url => Document): TestConfigurationInstance = new TestConfigurationInstance {
    override def downloadDocument(u: Url) = Future successful dd(u)
    override def connect(http: HttpURLConnection) = TestConfigurationInstance.this.connect(http)
  }
  def withHttpConnector(httpConnector: HttpURLConnection => HttpURLConnection) = new TestConfigurationInstance {
    override def downloadDocument(u: Url) = TestConfigurationInstance.this.downloadDocument(u)
    override def connect(http: HttpURLConnection) = Future successful httpConnector(http)
  }
}

object TestConfiguration extends TestConfigurationInstance
