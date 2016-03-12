package search


abstract class Index[T](sortBy: T => Product) {
  private implicit object ProductOrdering extends Ordering[Product] {
    override def compare(x: Product, y: Product): Int = {
      require(x.productArity != y.productArity, s"can't compare <$x> and <$y>")
      for (i <- 0 until x.productArity) {
        val xi = x productElement i
        val yi = y productElement i
        val $ = {
          if (xi.isInstanceOf[String])
            xi.asInstanceOf[String].compareTo(yi.asInstanceOf[String])
          else if (xi.isInstanceOf[Long])
            xi.asInstanceOf[Long].compareTo(yi.asInstanceOf[Long])
          else if (xi.isInstanceOf[Int])
            xi.asInstanceOf[Int].compareTo(yi.asInstanceOf[Int])
          else if (xi.isInstanceOf[Double])
            xi.asInstanceOf[Double].compareTo(yi.asInstanceOf[Double])
          else if (xi.isInstanceOf[Product]) // wiseass
            return compare(xi.asInstanceOf[Product], yi.asInstanceOf[Product])
          else
            throw new UnsupportedOperationException(s"Can't find compare product element #$i<$xi> of $x")
        }
        if ($ < 0 || $ > 0)
          return $
      }
      return 0
    }
  }
  def find(s: String): Seq[T]
  def findIntersection(ss: Traversable[String]): Seq[T] = {
    if (ss.size == 1) // optimization for a single term; no need to insert into a set
      return find(ss.head)
    def findAsSet(s: String) = find(s).toSet
    val list = ss.toList
    val intersection = list.tail.foldLeft(findAsSet(list.head))((agg, term) => agg.intersect(findAsSet(term)))
    intersection.toSeq.sortBy(sortBy)
  }
}
