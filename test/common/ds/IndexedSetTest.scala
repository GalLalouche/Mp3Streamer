package common.ds

import common.AuxSpecs
import org.scalatest.FreeSpec

import scalaz.Semigroup

class IndexedSetTest extends FreeSpec with AuxSpecs {
  "just work" in {
    case class Person(name: String, money: Int)
    implicit object PersonSemigroup extends Semigroup[Person] {
      override def append(f1: Person, f2: => Person): Person = f1.copy(money = f1.money + f2.money)
    }
    IndexedSet[String, Person](_.name) + Person("A", 10) + Person("B", 20) + Person("A", 5) shouldMultiSetEqual
        Vector(Person("A", 15), Person("B", 20))
  }
}
