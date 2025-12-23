package common.io

import backend.Retriever
import com.google.inject.{Inject, Provider}
import io.lemonlabs.uri.Url
import org.jsoup.nodes.Document
import play.api.libs.ws.StandaloneWSRequest

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.Duration

import common.io.RichWSResponse.richWSResponse
import common.io.WSAliases._
import common.rich.RichFuture._
import common.rich.RichT._

/** Things that talk to the outside world. Spo-o-o-o-ky IO! */
final class InternetTalker @Inject() (wsClientProvider: Provider[WSClient], ec: ExecutionContext) {
  private implicit def iec: ExecutionContext = ec

  def useWs[T](f: Retriever[WSClient, T]): Future[T] = {
    val client = wsClientProvider.get()
    val $ =
      try f(client)
      catch {
        // In case an error occurs while applying f
        case e: Throwable =>
          client.close()
          throw e
      }
    $.consumeTry(client.close().const)
  }

  def asBrowser[T](
      url: Url,
      f: WSRequest#Response => T,
      timeout: Duration = Duration.Zero,
  ): Future[T] =
    getBrowserAux(url, _.joinOption(timeout.optFilter(_.length > 0))(_.withRequestTimeout(_)))
      .map(f)
  def downloadDocument(url: Url, decodeUtf: Boolean = false): Future[Document] =
    asBrowser(url, _.document(decodeUtf))
  def get(url: Url): Future[WSResponse] = getAux(url)
  def getAsBrowser(url: Url): Future[WSResponse] = getBrowserAux(url)

  private def getAux(url: Url, f: StandaloneWSRequest => StandaloneWSRequest = identity) =
    useWs(_.url(url.toStringPunycode) |> f |> (_.get()))
  private def getBrowserAux(url: Url, f: StandaloneWSRequest => StandaloneWSRequest = identity) =
    getAux(url, f.compose(_.addHttpHeaders("user-agent" -> InternetTalker.AgentUrl)))
}

object InternetTalker {
  private val AgentUrl =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36"
}
