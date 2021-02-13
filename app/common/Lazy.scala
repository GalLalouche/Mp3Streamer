package common

import scalaz.Monad

/** A solution to the fact that a class cannot have lazy val constructor parameters. */
trait Lazy[+A] {
  def get: A
}
object Lazy {
  def apply[A](_a: => A): Lazy[A] = new Lazy[A] {
    override lazy val get = _a
  }
  implicit object MonadEv extends Monad[Lazy] {
    override def point[A](a: => A): Lazy[A] = Lazy(a)
    override def bind[A, B](fa: Lazy[A])(f: A => Lazy[B]): Lazy[B] = new Lazy[B] {
      override lazy val get = f(fa.get).get
    }
  }
}
