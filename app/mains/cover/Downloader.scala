package mains.cover

import java.net.URL

import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source

private class Downloader(implicit ec: ExecutionContext) {
  def download(url: String, encoding: String): Future[Array[Byte]] = Future({
    val $ = new URL(url).openConnection()
    $.setRequestProperty("user-agent", """user-agent:Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36""")
    Source.fromInputStream($.getInputStream, encoding)
      .map(_.toByte)
      .toArray
  })
}
