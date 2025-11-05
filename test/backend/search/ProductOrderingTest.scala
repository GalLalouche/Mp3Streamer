package backend.search

import backend.search.ProductOrdering.Impl
import common.test.AuxSpecs
import org.scalatest.freespec.AnyFreeSpec

class ProductOrderingTest extends AnyFreeSpec with AuxSpecs {
  private def product(a: Any*): Product = new Product {
    override def productElement(n: Int) = a(n)
    override val productArity = a.length
    override def canEqual(that: Any) = true
  }

  "Same" in {
    val p = product(1, 2L, "foo", 4.0, None, Some(4))
    p should be >= p
    p should be <= p
  }
  "String" in {
    product(1, 2, "foo", 3.0) should be > product(1, 2, "bar", 4.0)
  }
  "Long" in {
    product(1, 2, 2L, Math.PI) should be < product(1, 2, 3L, Math.E)
  }
  "Int" in {
    product(1, 2, 2, "foo") should be < product(1, 2, 3, "bar")
  }
  "Double" in {
    product(1, 2, 2.5, 4) should be < product(1, 2, 3.5, 3)
  }
  "Optional" - {
    "Two Nones" in {
      product(1, 2, None, 3) should be < product(1, 2, None, 4)
    }
    "Some and None" in {
      product(1, 2, Some(0), 2) should be > product(1, 2, None, 3)
    }
    "None and Some" in {
      product(1, 2, None, 3) should be < product(1, 2, Some(4L), 2)
    }
    "Two Somes" in {
      product(1, 2, Some("foo"), 2) should be > product(1, 2, Some("bar"), 3)
    }
  }
  "Product" in {
    product(1, 2, (1, 2, 3), 4) should be > product(1, 2, (1, 2, 2), 5)
  }
}
