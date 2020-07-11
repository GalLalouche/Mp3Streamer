package mains

import java.net.URLEncoder

import backend.Url

import scala.sys.process.Process

import common.rich.primitives.RichString._

object BrowserUtils {
  def pointBrowserTo(query: Url): Unit =
    Process("""C:\Users\Gal\AppData\Local\Google\Chrome\Application\chrome.exe """ + query.address.quote).!!
  def searchFor(query: String): Unit =
    pointBrowserTo(Url("https://www.google.com/search?q=" + URLEncoder.encode(query, "utf-8")))
}
