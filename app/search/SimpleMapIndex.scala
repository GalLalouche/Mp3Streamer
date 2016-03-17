package search

private class SimpleMapIndex[T: Indexable](map: Map[String, Seq[T]]) extends Index[T] {
  def find(s: String) = map.getOrElse(s.toLowerCase, Nil)
  override def sortBy(t: T): Product = implicitly[Indexable[T]].sortBy(t)
}
