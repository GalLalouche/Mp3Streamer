package common.io

import backend.Retriever
import com.google.inject.{Inject, Provider}
import io.lemonlabs.uri.Url
import org.jsoup.nodes.Document

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.Duration

import common.io.RichWSRequest._
import common.io.WSAliases._
import common.rich.RichFuture._
import common.rich.RichT._

/** Things that talk to the outside world. Spo-o-o-o-ky IO! */
class InternetTalker @Inject() (wsClientProver: Provider[WSClient], ec: ExecutionContext) {
  private implicit def iec: ExecutionContext = ec

  def useWs[T](f: Retriever[WSClient, T]): Future[T] = {
    val client = wsClientProver.get()
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

  // TODO: handle code duplication.
  final def asBrowser[T](
      url: Url,
      f: Retriever[WSRequest, T],
      timeout: Duration = Duration.Zero,
  ): Future[T] = useWs(
    f.compose(
      _.url(url.toStringPunycode)
        .addHttpHeaders("user-agent" -> InternetTalker.AgentUrl)
        .joinOption(timeout.optFilter(_.length > 0))(_.withRequestTimeout(_)),
    ),
  )
  final def downloadDocument(url: Url, decodeUtf: Boolean = false): Future[Document] =
    asBrowser(url, _.document(decodeUtf))
  final def get(url: Url): Future[WSResponse] = useWs(_.url(url.toStringPunycode).get())
  final def getAsBrowser(url: Url): Future[WSResponse] =
    useWs(_.url(url.toStringPunycode).addHttpHeaders("user-agent" -> InternetTalker.AgentUrl).get())
}

object InternetTalker {
  private val AgentUrl =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36"
}
