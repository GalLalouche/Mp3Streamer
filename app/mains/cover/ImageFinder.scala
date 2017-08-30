package mains.cover

import backend.Url
import common.io.InternetTalker
import common.io.RichWSRequest._

import scala.concurrent.Future

private class ImageFinder(implicit it: InternetTalker) {
  def find(url: Url): Future[Seq[UrlSource]] = it.asBrowser(url, _.string) map parse
  def parse(html: String): Seq[UrlSource] = {
    """"ou":"[^"]+"""".r
        .findAllIn(html) // assumes format "ou":"<url>". fucking closure :\
        .map(_.dropWhile(_ != ':').drop(2).dropRight(1))
        .toVector
        .map(e => UrlSource(Url(e)))
  }
}
