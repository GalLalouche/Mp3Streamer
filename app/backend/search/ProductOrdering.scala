package backend.search

import scala.annotation.tailrec

import common.rich.primitives.RichOption._

private object ProductOrdering {
  implicit object Impl extends Ordering[Product] {
    @tailrec
    private def compareElement(xi: Any, yi: Any): Option[Int] = {
      xi match {
        case s: String => Some(s compareTo yi.asInstanceOf[String])
        case l: Long => Some(l compareTo yi.asInstanceOf[Long])
        case i1: Int => Some(i1 compareTo yi.asInstanceOf[Int])
        case d: Double => Some(d compareTo yi.asInstanceOf[Double])
        case xo: Option[Any] =>
          val yo = yi.asInstanceOf[Option[Any]]
          if (xo.isEmpty && yo.isEmpty)
            Some(0)
          else if (xo.isEmpty)
            Some(-1)
          else if (yo.isEmpty)
            Some(1)
          else
            compareElement(xo.get, yo.get)
        case x1: Product => Some(compare(x1, yi.asInstanceOf[Product]))
        case _ => None
      }
    }
    override def compare(x: Product, y: Product): Int = {
      require(x.productArity == y.productArity, s"can't compare <$x> and <$y> of different arity")
      0.until(x.productArity).iterator.map {i =>
        val xi = x productElement i
        val yi = y productElement i
        compareElement(xi, yi).getOrThrow(
          new UnsupportedOperationException(s"Can't compare product element #$i<$xi> of <$x> with <$y>"))
      }.find(_ != 0).getOrElse(0)
    }
  }
}
