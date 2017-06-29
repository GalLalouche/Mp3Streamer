package common.io

import java.net.HttpURLConnection

import backend.Url
import org.jsoup.nodes.Document

import scala.concurrent.Future

/** Things that talk to the outside world */
trait InternetTalker {
  def downloadDocument(url: Url): Future[Document]
  /**
    * Connects to the given URL. For testing, might return a new type, e.g., for mocks, so use the
    * returned object.
    */
  def connect(url: Url, config: HttpURLConnection => Unit = _ => ()): Future[HttpURLConnection]
}
