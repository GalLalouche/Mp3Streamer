package common.io

import java.net.HttpURLConnection

import backend.Url
import common.rich.RichT._
import org.apache.commons.io.IOUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

/** Things that talk to the outside world */
trait InternetTalker extends ExecutionContext {
  private implicit val ec: ExecutionContext = this
  def config(c: HttpURLConnection => Unit): InternetTalker = new InternetTalker {
    val that = InternetTalker.this
    override protected def connection(url: Url) = that connection url applyAndReturn c
    override def execute(runnable: Runnable): Unit = that.execute(runnable)
    override def reportFailure(cause: Throwable): Unit = that.reportFailure(cause)
    override def ws = that.ws
  }
  def asBrowser: InternetTalker = config(_.setRequestProperty("user-agent",
    """user-agent:Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36"""))
  final def bytes(url: Url): Future[Array[Byte]] = {
    connect(url).map(_.getInputStream).map(IOUtils.toByteArray)
  }
  final def downloadDocument(url: Url): Future[Document] = bytes(url).map(new String(_, "UTF-8")).map(Jsoup.parse)
  protected def connection(url: Url): HttpURLConnection
  def connect(url: Url): Future[HttpURLConnection] = Future {connection(url)}
  def ws: WSClient
  def get(u: Url) = ws.url(u.address).get()
}
