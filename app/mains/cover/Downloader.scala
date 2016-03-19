package mains.cover

import java.net.URL

import scala.io.Source

private class Downloader {
  def download(url: String, encoding: String): Array[Byte] = {
    val $ = new URL(url).openConnection()
    $.setRequestProperty("user-agent", """user-agent:Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36""")
    Source.fromInputStream($.getInputStream, encoding)
      .map(_.toByte)
      .toArray
  }
}
