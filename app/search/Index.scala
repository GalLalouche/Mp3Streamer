package search

class Index[T: Indexable](map: Map[String, Seq[T]]) {
  def find(s: String) = map.get(s.toLowerCase).getOrElse(Nil)
  def findIntersection(ss: Traversable[String]): Seq[T] = {
    if (ss.size == 1) // optimization for a single term; no need to insert into a set
      return find(ss.head)
    def findAsSet(s: String) = find(s).toSet
    val list = ss.toList
    val intersection = list.tail.foldLeft(findAsSet(list.head))((agg, term) => agg.intersect(findAsSet(term)))
    implicitly[Indexable[T]].sort(intersection.toVector)
  }
}
