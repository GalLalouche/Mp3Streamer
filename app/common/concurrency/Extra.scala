package common.concurrency

/** An actor with no input or output */
trait Extra extends SimpleActor[Unit] {
  // overloads to avoid passing in an explicit unit
  def apply(): Unit
  final override protected def apply(u: Unit) { this.apply() }
  final def !() { this.!(()) }
}
