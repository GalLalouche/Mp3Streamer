package mains

import scala.sys.process.Process

import common.Urls
import io.lemonlabs.uri.Url

private object BrowserUtils {
  private val Query = "q"
  def pointBrowserTo(query: Url): Unit =
    Process(
      """C:\Program Files\Google\Chrome\Application\chrome.exe """ + query.toStringPunycode,
    ).!!
  def searchFor(query: String): Unit = pointBrowserTo(Urls.googleSearch(query))
  def searchForLucky(query: String): Unit = pointBrowserTo(Urls.searchForLucky(query))
}
