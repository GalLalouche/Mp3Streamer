package mains

import io.lemonlabs.uri.Url

import scala.sys.process.Process

private object BrowserUtils {
  private val Query = "q"
  def pointBrowserTo(query: Url): Unit =
    Process(
      """C:\Program Files\Google\Chrome\Application\chrome.exe """ + query.toStringPunycode,
    ).!!
  def searchFor(query: String): Unit =
    pointBrowserTo(Url.parse("https://www.google.com/search").withQueryString(Query -> query))
  /* Similar to the above, but enters the first link. */
  def searchForLucky(query: String): Unit =
    // Since Google's feeling lucky is borken, use DuckDuckGo's.
    pointBrowserTo(Url.parse("https://duckduckgo.com/").withQueryString(Query -> ("\\" + query)))
}
