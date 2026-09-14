package common.concurrency.actor

import scala.concurrent.{ExecutionContext, Future}

import cats.implicits.toFunctorOps

import common.concurrency.DaemonExecutionContext

private abstract class SimpleTypedActorTemplate[Msg, +Result](override val name: String)
    extends SimpleTypedActor[Msg, Result] {
  implicit val ec: ExecutionContext = DaemonExecutionContext.single(name)

  final def void: SimpleActor[Msg] = new SimpleActor[Msg] {
    override def !(m: => Msg): Future[Unit] = SimpleTypedActorTemplate.this.!(m).void
    override def name: String = SimpleTypedActorTemplate.this.name
  }
}
