package common.concurrency

/** An actor with neither input nor output. */
trait AbstractExtra extends Extra {
  // overloads to avoid passing in an explicit unit
  def apply(): Unit
  final override protected def apply(u: Unit): Unit = {this.apply()}
}
