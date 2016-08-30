package common

import org.scalatest.FreeSpec
import org.scalatest.matchers.ShouldMatchers

class IndexedSetTest extends FreeSpec with AuxSpecs {
  "just work" in {
    case class Person(name: String, money: Int)
    val $ = IndexedSet.apply[String, Person](_.name, (e1, e2) => Person(e1.name, e1.money + e2.money))
    ($ + Person("A", 10) + Person("B", 20) + Person("A", 5)).toSet shouldReturn Set(Person("A", 15), Person("B", 20))
  }
}
