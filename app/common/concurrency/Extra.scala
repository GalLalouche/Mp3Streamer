package common.concurrency

import scala.concurrent.Future

/** An actor with neither input nor output. Also provides an overload to avoid passing Unit explicitly. */
trait Extra extends SimpleActor[Unit] {
  final def !(): Future[Unit] = this.!(())
}

object Extra {
  def apply(name: String, f: => Any): Extra = new SimpleTypedActorImpl[Unit, Unit](name, _ => f) with Extra
}
