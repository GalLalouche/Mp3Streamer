package search

trait IndexBuilder {
  def buildIndexFor[T:Indexable](songs: TraversableOnce[T]): Index[T]
}
