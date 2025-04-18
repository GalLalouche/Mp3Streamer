package backend.lucky

import com.google.inject.Inject
import formatter.UrlDecoder

import scala.concurrent.{ExecutionContext, Future}

/**
 * The reason for this nonsense is that for some reason, I can't have an href that links to a
 * DuckDuckGo or Google's "I'm feeling lucky" quick search. So this is I'm feeling lucky as a
 * service, which does the traversal in the backend.
 */
class LuckyFormatter @Inject() (
    $ : DuckDuckgoFetcher,
    decoder: UrlDecoder,
    ec: ExecutionContext,
) {
  private implicit val iec: ExecutionContext = ec
  def search(query: String): Future[String] = $.search(query).map(decoder.apply)
}
