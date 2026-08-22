package common.concurrency

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.kats.ToMoreMonadErrorOps.toMoreApplicativeErrorOps

/**
 * An actor with neither input nor output, i.e., an object you can tell "go do that thing you do".
 */
sealed trait Extra extends SimpleActor[Unit] {
  def !(): Future[Unit]
}

object Extra {
  def apply(name: String)(f: => Any): Extra =
    new SimpleTypedActorImpl[Unit, Unit](name, _ => f) with Extra {
      override def !(): Future[Unit] = go(this, name)
    }

  def unique(name: String)(f: => Any): Extra =
    new UniqueSimpleTypedActorImpl[Unit, Unit](name, _ => f) with Extra {
      override def !(): Future[Unit] = go(this, name)
      protected override def describeMessage(m: Unit): String = "Extra request"
    }

  private def go(actor: SimpleTypedActor[Unit, Unit], name: String)(implicit
      ec: ExecutionContext,
  ): Future[Unit] =
    actor
      .!(())
      // Since it's possible no one will use the Extra output, it's important to log this!
      .listenError(scribe.error(s"Extra <$name> failed", _))
}
