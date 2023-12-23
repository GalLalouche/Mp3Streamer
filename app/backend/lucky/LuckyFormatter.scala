package backend.lucky

import javax.inject.Inject
import scala.concurrent.Future

import io.lemonlabs.uri.Url

/**
 * The reason for this nonsense is that for some reason, I can't have an href that links to a
 * DuckDuckGo or Google's "I'm feeling lucky" quick search. So this is I'm feeling lucky as a
 * service, which does the traversal in the backend.
 */
private class LuckyFormatter @Inject() ($ : DuckDuckgoFetcher) {
  def search(query: String): Future[String] = $.search(query)
}
