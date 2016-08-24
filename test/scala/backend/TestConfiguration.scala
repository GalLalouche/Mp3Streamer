package backend

import java.net.HttpURLConnection

import models.MusicFinder
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import slick.driver.{H2Driver, JdbcProfile}

import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source

class TestConfigurationInstance extends Configuration {
  override implicit val ec: ExecutionContext = new ExecutionContext {
    override def reportFailure(cause: Throwable): Unit = ???
    override def execute(runnable: Runnable): Unit = runnable.run()
  }

  override implicit val driver: JdbcProfile = H2Driver
  override implicit val db: driver.backend.DatabaseDef =
    driver.api.Database.forURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.H2.JDBC")
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
