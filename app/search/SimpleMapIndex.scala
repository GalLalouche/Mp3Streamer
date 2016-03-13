package search

private class SimpleMapIndex[T: Indexable](map: Map[String, Seq[T]]) extends Index[T](implicitly[Indexable[T]].sortBy) {
  def find(s: String) = map.getOrElse(s.toLowerCase, Nil)
}
