package common.io

import backend.Url
import common.io.RichWSRequest._
import common.rich.RichT._
import org.jsoup.nodes.Document
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}

import scala.concurrent.{ExecutionContext, Future}

/** Things that talk to the outside world */
trait InternetTalker extends ExecutionContext {
  private implicit val ec: ExecutionContext = this

  final def asBrowser[T](url: Url, f: WSRequest => Future[T]): Future[T] =
    useWs(_.url(url.address).withHeaders("user-agent" ->
        "user-agent:Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36") |> f)
  final def downloadDocument(url: Url): Future[Document] = asBrowser(url, _.document)
  final def get(url: Url): Future[WSResponse] = useWs(_.url(url.address).get())
  final def useWs[T](f: WSClient => Future[T]): Future[T] = {
    val $ = createWsClient()
    try
      f($).andThen({ $.close() }.partialConst)
    catch {
      // In case an error occurs while applying f
      case e: Throwable =>
        $.close()
        throw e
    }
  }
  protected def createWsClient(): WSClient
}
