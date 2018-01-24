package common.io

import backend.Url
import backend.logging.Logger
import common.io.RichWSRequest._
import common.io.WSAliases._
import common.rich.RichFuture._
import common.rich.RichT._
import org.jsoup.nodes.Document

import scala.concurrent.{ExecutionContext, Future}

/** Things that talk to the outside world. Spo-o-o-o-ky IO! */
trait InternetTalker extends ExecutionContext {
  private implicit val ec: ExecutionContext = this

  private val agentUrl =
    "user-agent:Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36"
  final def asBrowser[T](url: Url, f: WSRequest => Future[T]): Future[T] =
    useWs(_.url(url.address).addHttpHeaders("user-agent" -> agentUrl) |> f)
  final def downloadDocument(url: Url): Future[Document] = asBrowser(url, _.document)
  final def get(url: Url): Future[WSResponse] = useWs(_.url(url.address).get())
  def useWs[T](f: WSClient => Future[T]): Future[T] = {
    val client = createWsClient()
    val $ =
      try f(client)
      catch {
        // In case an error occurs while applying f
        case e: Throwable =>
          client.close()
          throw e
      }
    $ consumeTry client.close().const
  }
  protected def createWsClient(): WSClient
  // TODO come up with a better hierarchy :\
  def logger: Logger
}
