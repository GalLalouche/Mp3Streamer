package search

trait Index[T] {
  def find(s: String): Seq[T] = findIntersection(List(s))
  def findIntersection(ss: Traversable[String]): Seq[T]
}
