package common.concurrency.actor

import scala.concurrent.ExecutionContext

import cats.implicits.toFunctorOps

import common.concurrency.DaemonExecutionContext

private abstract class SimpleTypedActorTemplate[Msg, +Result](name: String)
    extends SimpleTypedActor[Msg, Result] {
  implicit val ec: ExecutionContext = DaemonExecutionContext.single(name)

  final def void: SimpleActor[Msg] = SimpleTypedActorTemplate.this.!(_).void
}
