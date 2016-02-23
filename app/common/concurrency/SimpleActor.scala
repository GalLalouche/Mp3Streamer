package common.concurrency

import common.rich.RichT._

/** 
 * Since akka's is such a pain in import common.concurrency.FutureFactory
the ass.
 * 
 * Simple actors are a type-safe DSL for asynchronous, single-threaded tasks. All threads are daemon. 
 * You probably <b>don't</b> want to compose actors, i.e., let one actor invoke another actor. Since 
 * the first actor is already running in its own thread, you can probably away with invoking 
 * whatever computation as part of its context.
 */
trait SimpleActor[Msg] extends FutureFactory[Msg, Unit]