package backend.search

private trait Index[T] {
  def find(s: String): Seq[T] = findIntersection(Vector(s))
  def findIntersection(ss: Iterable[String]): Seq[T]
}
