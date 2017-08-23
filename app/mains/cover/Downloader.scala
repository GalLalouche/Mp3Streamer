package mains.cover

import backend.Url
import common.io.InternetTalker

import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source

private class Downloader(implicit ec: ExecutionContext, it: InternetTalker) {
  def download(url: Url, encoding: String): Future[Array[Byte]] = {
    val connection = it.config(_.setRequestProperty("user-agent",
      """user-agent:Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36"""))
        .connect(url)
    connection.map(c =>
      Source.fromInputStream(c.getInputStream, encoding)
          .map(_.toByte)
          .toArray)
  }
}
