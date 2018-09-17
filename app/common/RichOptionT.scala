package common

import scala.language.{higherKinds, reflectiveCalls}

import scalaz.{~>, Applicative, Bind, Functor, IdInstances, Monad, OptionT}
import scalaz.syntax.ToMonadOps

// TODO move to common
object RichOptionT extends ToMonadOps with IdInstances {
  implicit class richOptionT[F[_], A](private val $: OptionT[F, A]) extends AnyVal {
    def subFlatMap[B](f: A => Option[B])(implicit F: Functor[F]): OptionT[F, B] =
      OptionT($.run.map(_ flatMap f))
  }

  // TODO move ToMoreFunctorOps/wherever
  def toOptionT[F[_] : Functor, A, B](fa: F[A])(f: A => Option[B]): OptionT[F, B] = OptionT(fa.map(f))
  def toOptionTF[F[_] : Bind, A, B](fa: F[A])(f: A => F[Option[B]]): OptionT[F, B] = OptionT(fa.flatMap(f))
  def toOptionTF2[F[_] : Monad, A, B](fa: F[A])(f: A => OptionT[F, B]): OptionT[F, B] =
    OptionT(fa.map(Option(_))).flatMap(f)
  // Find a way to solve this using ~>
  class OptionTApp[F[_] : Applicative] {
    def apply[A](o: Option[A]): OptionT[F, A] = OptionT(o.point)
  }
  // Right...
  def app[F[_] : Applicative]: Option ~> ({type λ[α] = OptionT[F, α]})#λ =
    new (Option ~> ({type λ[α] = OptionT[F, α]})#λ) {
      override def apply[A](fa: Option[A]) = OptionT(fa.point)
    }
}
