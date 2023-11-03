package common

/**
 * A partial function that can mutated to support new arguments. Can be more useful than a mutable
 * Map since it also supports a mutating const (i.e., default) changes.
 */
// TODO move to common
class MutablePartialFunction[A, B] extends PartialFunction[A, B] {
  private var pf: PartialFunction[A, B] = PartialFunction.empty
  def +=(pf: PartialFunction[A, B]): this.type = {
    this.pf = this.pf.orElse(pf)
    this
  }
  def const(b: B): this.type = this.+= { case _ => b }
  def clear(): this.type = {
    this.pf = PartialFunction.empty
    this
  }
  override def isDefinedAt(x: A) = pf.isDefinedAt(x)
  override def apply(v1: A) = pf(v1)
}

object MutablePartialFunction {
  def empty[A, B]: MutablePartialFunction[A, B] = new MutablePartialFunction
  def apply[A, B](pf: PartialFunction[A, B]): MutablePartialFunction[A, B] = empty += pf
}
