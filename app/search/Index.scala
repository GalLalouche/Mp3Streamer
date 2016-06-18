package search

trait Index[T] {
  def find(s: String): Seq[T] = findIntersection(List(s))
  def findIntersection(ss: Traversable[String]): Seq[T]
}
object Index {
  implicit object ProductOrdering extends Ordering[Product] {
    override def compare(x: Product, y: Product): Int = {
      require(x.productArity == y.productArity, s"can't compare <$x> and <$y>")
      for (i <- 0 until x.productArity) {
        val xi = x productElement i
        val yi = y productElement i
        val $ = {
          xi match {
            case s: String => s compareTo yi.asInstanceOf[String]
            case l: Long => l compareTo yi.asInstanceOf[Long]
            case i1: Int => i1 compareTo yi.asInstanceOf[Int]
            case d: Double => d compareTo yi.asInstanceOf[Double]
            case x1: Product => return compare(x1, yi.asInstanceOf[Product])
            case _ => throw new UnsupportedOperationException(s"Can't compare product element #$i<$xi> of $x")
          }
        }
        if ($ < 0 || $ > 0)
          return $
      }
      0
    }
  }
}
