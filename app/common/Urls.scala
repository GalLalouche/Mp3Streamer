package common

import _root_.io.lemonlabs.uri.Url

object Urls {
  private val Query = "q"

  def googleSearch(query: String): Url =
    Url.parse("https://www.google.com/search").withQueryString(Query -> query)

  // Since Google's feeling lucky is borken, use DuckDuckGo's.
  def searchForLucky(query: String): Url = // Starting with backslash means "I think I'm lucky".
    Url.parse(s"https://duckduckgo.com/").withQueryString(Query -> ("\\" + query))
}
