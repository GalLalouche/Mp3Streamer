package common.concurrency.actor

import scala.concurrent.ExecutionContext

import common.concurrency.DaemonExecutionContext

private abstract class ActorTemplate[Msg, +Result](override val name: String)
    extends Actor[Msg, Result] {
  implicit val ec: ExecutionContext = DaemonExecutionContext.single(name)
}
