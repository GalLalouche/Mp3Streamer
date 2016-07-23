package search

import search.Index.ProductOrdering

trait IndexImpl[T] extends Index[T] {
  def find(s: String): Seq[T]
  def sortBy(t: T): Product
  def findIntersection(ss: Traversable[String]): Seq[T] = {
    if (ss.size == 1) // optimization for a single term; no need to insert into a set
      return find(ss.head)
    def findAsSet(s: String) = find(s).toSet
    val list = ss.toList
    val intersection = list.tail.foldLeft(findAsSet(list.head))((agg, term) => agg.intersect(findAsSet(term)))
    intersection.toSeq.sortBy(sortBy)
  }
}
