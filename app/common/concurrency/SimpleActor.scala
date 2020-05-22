package common.concurrency

import scala.concurrent.Future

/** 
 * Since Akka's is such a pain in the ass.
 * 
 * Simple actors are a type-safe DSL for asynchronous, single-threaded tasks. All threads are daemon. 
 */
trait SimpleActor[Msg] extends SimpleTypedActor[Msg, Unit]
object SimpleActor {
  def async[Msg](name: String, f: Msg => Future[Any]): SimpleActor[Msg] =
    new SimpleTypedActorAsyncImpl(name, f).void
}
