package mains.cover.image

import mains.cover.UrlSource

import scala.concurrent.Future

private[cover] trait ImageAPISearch {
  def apply(terms: String): Iterator[Future[UrlSource]]
}
