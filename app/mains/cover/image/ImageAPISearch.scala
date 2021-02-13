package mains.cover.image

import mains.cover.ImageSource

import common.concurrency.FutureIterant

private[cover] trait ImageAPISearch {
  def apply(terms: String): FutureIterant[ImageSource]
}
