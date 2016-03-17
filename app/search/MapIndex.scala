package search

private class MapIndex[T: Indexable](map: Map[String, Seq[T]]) extends IndexImpl[T] {
  override def find(s: String) = map.getOrElse(s.toLowerCase, Nil)
  override def sortBy(t: T): Product = implicitly[Indexable[T]].sortBy(t)
}
