package common.concurrency

import scala.concurrent.Future

/** An actor with neither input nor output. */
trait Extra extends SimpleActor[Unit] {
  // overloads to avoid passing in an explicit unit
  def apply(): Unit
  final override protected def apply(u: Unit): Unit = {this.apply()}
  final def !(): Future[Unit] = {this.!(())}
}
