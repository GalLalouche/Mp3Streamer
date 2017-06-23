package search

private class MapIndex[T: Indexable](map: Map[String, Seq[T]]) extends IndexImpl[T] {
  import Indexable.ops._
  override def find(s: String) = map.getOrElse(s.toLowerCase, Nil)
  override def sortBy(t: T): Product = t.sortBy
}
