package search
import scala.UnsupportedOperationException


abstract class Index[T](sortBy: T => Product) {
  private implicit object ProductOrdering extends Ordering[Product] {
    override def compare(x: Product, y: Product): Int = {
      require(x.productArity != y.productArity, s"can't compare <$x> and <$y>")
      for (i <- 0 until x.productArity) {
        val xi = x productElement i
        val yi = y productElement i
        val $ = {
          xi match {
            case s: String => s.compareTo(yi.asInstanceOf[String])
            case l: Long => l.compareTo(yi.asInstanceOf[Long])
            case i1: Int => i1.compareTo(yi.asInstanceOf[Int])
            case d: Double => d.compareTo(yi.asInstanceOf[Double])
            case p: Product => return compare(p, yi.asInstanceOf[Product])
            case _ => throw new UnsupportedOperationException(s"Can't find compare product element #$i<$xi> of $x")
          }
        }
        if ($ < 0 || $ > 0)
          return $
      }
      0
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
