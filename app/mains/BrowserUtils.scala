package mains

import io.lemonlabs.uri.Url

import scala.sys.process.Process

import common.Urls

private object BrowserUtils {
  def pointBrowserTo(query: Url): Unit =
    Process(
      """C:\Program Files\Google\Chrome\Application\chrome.exe """ + query.toStringPunycode,
    ).!!
  def searchFor(query: String): Unit = pointBrowserTo(Urls.googleSearch(query))
  def searchForLucky(query: String): Unit = pointBrowserTo(Urls.searchForLucky(query))
}
