package common

import scala.collection.GenTraversable

trait Collectable[T, S] {
  def +(s: S, t: T): S
  def empty: S
}

object Collectable {
  def fromList[T, S](xs: GenTraversable[T])(implicit s: Collectable[T, S]): S =
    xs./:(implicitly[Collectable[T, S]].empty)(implicitly[Collectable[T, S]].+)
}
