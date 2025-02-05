package common.concurrency

import scala.concurrent.Future

/**
 * Since Akka's is such a pain in the ass.
 *
 * Simple actors are a type-safe DSL for asynchronous, single-threaded tasks. All threads are
 * daemon. Note that this isn't a proper actor in the Erlang sense: every actor runs on its own
 * thread, so you can't have hundreds of thousands of actors around.
 */
trait SimpleActor[Msg] extends SimpleTypedActor[Msg, Unit]
object SimpleActor {
  def apply[Msg](name: String, f: Msg => Any): SimpleActor[Msg] =
    new SimpleTypedActorImpl(name, f).void
  def async[Msg](name: String, f: Msg => Future[Any]): SimpleActor[Msg] =
    new SimpleTypedActorAsyncImpl(name, f).void
}
