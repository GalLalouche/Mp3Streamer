package common.concurrency

import scala.concurrent.Future

/** An actor with neither input nor output, i.e., an object you can tell "go do that thing you do". */
trait Extra extends SimpleActor[Unit] {
  final def !(): Future[Unit] = this.!(())
}

object Extra {
  def apply(name: String, f: => Any): Extra = new SimpleTypedActorImpl[Unit, Unit](name, _ => f) with Extra
}
