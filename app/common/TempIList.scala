package common

import cats.data.Nested

object TempIList {
  type ListT[F[_], A] = Nested[F, List, A]

  def ListT[F[_], A](fa: F[List[A]]): ListT[F, A] = Nested.apply(fa)
}
