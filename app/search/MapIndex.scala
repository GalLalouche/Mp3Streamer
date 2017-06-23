package search

private class MapIndex[T: Indexable](map: Map[String, Seq[T]]) extends IndexImpl[T] with ToIndexableOps {
  override def find(s: String) = map.getOrElse(s.toLowerCase, Nil)
  override def sortBy(t: T): Product = t.sortBy
}
