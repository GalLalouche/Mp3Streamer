package common.concurrency

import scala.concurrent.Future

import common.rich.func.BetterFutureInstances._
import common.rich.func.ToMoreMonadErrorOps.toMoreMonadErrorOps

/**
 * An actor with neither input nor output, i.e., an object you can tell "go do that thing you do".
 */
sealed trait Extra extends SimpleActor[Unit] {
  def !(): Future[Unit]
}

object Extra {
  def apply(name: String)(f: => Any): Extra =
    new SimpleTypedActorImpl[Unit, Unit](name, _ => f) with Extra {
      override def !(): Future[Unit] = this
        .!(())
        // Since it's possible no one will use the Extra output, it's important to log this!
        .listenError(scribe.error(s"Extra <$name> failed", _))
    }
}
