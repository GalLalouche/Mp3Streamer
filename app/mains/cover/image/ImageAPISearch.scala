package mains.cover.image

import common.concurrency.FutureIterant
import mains.cover.ImageSource

private[cover] trait ImageAPISearch {
  def apply(terms: String): FutureIterant[ImageSource]
}
