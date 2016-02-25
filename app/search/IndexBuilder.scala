package search

import models.Song

trait IndexBuilder {
  def buildIndexFor[T:Indexable](songs: TraversableOnce[T]): Index[T]
}