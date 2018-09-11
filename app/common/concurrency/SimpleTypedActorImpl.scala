package common.concurrency

import common.rich.RichT._
import common.rich.func.ToMoreFunctorOps
import common.rich.primitives.RichBoolean._

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.FutureInstances

/** It's a single threaded future factory basically. */
private class SimpleTypedActorImpl[Msg, Result](name: String, f: Msg => Result, unique: Boolean)
    extends SimpleTypedActor[Msg, Result]
        with ToMoreFunctorOps with FutureInstances {
  // TODO move uniqueness to its own class instead of piggybacking this one?
  private val messages: mutable.Map[Msg, Future[Result]] = new mutable.HashMap()
  private val singleThreadedJobQueue = new SingleThreadedJobQueue(name)
  private implicit val service: ExecutionContext = singleThreadedJobQueue.asExecutionContext
  private def clear(m: Msg): Unit = synchronized {messages.remove(m).ensuring(_.isDefined)}
  def !(m: => Msg): Future[Result] = synchronized {
    lazy val $ = Future(f(m))
    if (unique) {
      if (messages.contains(m).isFalse)
        messages.put(m, $.listen(clear(m).const))
      messages(m)
    } else
      $
  }
}
