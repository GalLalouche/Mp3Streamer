package common.concurrency

import scala.concurrent.Future
import common.rich.RichT._

/** An actor with neither input nor output. Also provides overloads to avoid passing Unit explicitly. */
trait Extra extends SimpleActor[Unit] {
  final def !(): Future[Unit] = {this.!(())}
}

object Extra {
  def apply(name: String, f: => Any): Extra =
    new SimpleTypedActorImpl[Unit, Unit](name, _ => f, false) with Extra
}
