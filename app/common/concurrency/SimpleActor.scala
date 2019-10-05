package common.concurrency

/** 
 * Since Akka's is such a pain in the ass.
 * 
 * Simple actors are a type-safe DSL for asynchronous, single-threaded tasks. All threads are daemon. 
 * You probably ''don't'' want to compose actors, i.e., let one actor invoke another actor.
 *
 * Since the first actor is already running in its own thread, you can probably get away with invoking
 * whatever computation as part of its context.
 */
trait SimpleActor[Msg] extends SimpleTypedActor[Msg, Unit]
