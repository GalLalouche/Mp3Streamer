package common

import common.Trither.{TriLeft, TriMid, TriRight}

// TODO move to ScalaCommon
sealed trait Trither[+A, +B, +C] {
  def fold[R](fa: A => R, fb: B => R, fc: C => R): R = this match {
    case Trither.TriLeft(a) => fa(a)
    case Trither.TriMid(b) => fb(b)
    case Trither.TriRight(c) => fc(c)
  }
  def trimap[D, E, F](fa: A => D, fb: B => E, fc: C => F): Trither[D, E, F] = this match {
    case Trither.TriLeft(a) => TriLeft(fa(a))
    case Trither.TriMid(b) => TriMid(fb(b))
    case Trither.TriRight(c) => TriRight(fc(c))
  }
  def tritherStrengthenLeft[D](d: D): Trither[(A, D), (B, D), (C, D)] =
    trimap(_ -> d, _ -> d, _ -> d)
  def tritherStrengthenRight[D](d: D): Trither[(D, A), (D, B), (D, C)] =
    trimap(d -> _, d -> _, d -> _)
}
object Trither {
  final case class TriLeft[A](a: A) extends Trither[A, Nothing, Nothing]
  final case class TriMid[B](b: B) extends Trither[Nothing, B, Nothing]
  final case class TriRight[C](c: C) extends Trither[Nothing, Nothing, C]
}
