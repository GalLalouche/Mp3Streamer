package common.ds

import scala.collection.GenTraversable
import scalaz.Semigroup
import scalaz.syntax.ToSemigroupOps

trait SemiCollectable[T, S] { // a semigroup that's a collectable
  def +(s: S, t: T): S
  def pure(t: T): S
}
trait Collectable[T, S] extends SemiCollectable[T, S] {
  def empty: S
  override def pure(t: T): S = this.+(empty, t)
}

object Collectable {
  implicit def listCollectable[T]: Collectable[T, List[T]] = new Collectable[T, List[T]] {
    override def +(s: List[T], t: T): List[T] = t :: s
    override def empty: List[T] = List()
  }
  implicit def setCollectable[T]: Collectable[T, Set[T]] = new Collectable[T, Set[T]] {
    override def +(s: Set[T], t: T): Set[T] = s + t
    override def empty: Set[T] = Set()
  }
  implicit def semigroupCollectable[T: Semigroup] = new SemiCollectable[T, T] with ToSemigroupOps {
    override def +(s: T, t: T): T = s |+| t
    override def pure(t: T): T = t
  }
  def fromList[T, S](xs: GenTraversable[T])(implicit s: Collectable[T, S]): S = xs./:(s.empty)(s.+)
}
