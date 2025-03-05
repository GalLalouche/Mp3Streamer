package mains

import io.lemonlabs.uri.Url

import scala.sys.process.Process

import common.Urls

private object BrowserUtils {
  def pointBrowserTo(query: Url): Unit = Process(
    """"C:\Program Files\Mozilla Firefox\firefox.exe" -new-tab """ + query.toStringPunycode,
  ).!!
  def searchFor(query: String): Unit = pointBrowserTo(Urls.googleSearch(query))
  def searchForLucky(query: String): Unit = pointBrowserTo(Urls.searchForLucky(query))
}
