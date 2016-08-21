package common.io

import java.net.HttpURLConnection

import backend.Url
import org.jsoup.nodes.Document

import scala.concurrent.Future

/** Things that talk to the outside world */
trait InternetTalker {
  /** Downloads an HTML document */
  def downloadDocument(url: Url): Future[Document]
  /**
  * Creates an HTTP connection
  * @param modify modifies the HttpUrlConnection, e.g., to add headers
*/
  def httpUrlConnection(url: Url, modify: HttpURLConnection => Unit): Future[HttpURLConnection]
}
