package common

import common.rich.primitives.RichBoolean

// TODO ScalaCommon
trait Filter[-A] {
  def passes(a: A): Boolean
  def and[B <: A](other: Filter[B]): Filter[B] = RichBoolean.and(passes, other.passes).apply
  def &&[B <: A](other: Filter[B]): Filter[B] = and(other)
  def or[B <: A](other: Filter[B]): Filter[B] = RichBoolean.or(passes, other.passes).apply
  def ||[B <: A](other: Filter[B]): Filter[B] = or(other)
  def negate: Filter[A] = RichBoolean.negate(passes).apply
  def ! : Filter[A] = negate
}

object Filter {
  def always: Filter[Any] = _ => true
  def never: Filter[Any] = _ => false
}
