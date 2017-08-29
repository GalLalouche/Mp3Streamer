package common.io

import java.net.HttpURLConnection

import backend.Url
import common.io.RichWSRequest._
import common.rich.RichT._
import org.jsoup.nodes.Document
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}

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
  def asBrowser(url: Url): WSRequest = ws.url(url.address).withHeaders("user-agent" ->
    "user-agent:Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36")
  def downloadDocument(url: Url): Future[Document] = asBrowser(url).document
  def get(url: Url): Future[WSResponse] = ws.url(url.address).get()
  protected def connection(url: Url): HttpURLConnection
  def ws: WSClient
}
