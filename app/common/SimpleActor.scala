package common

import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import common.rich.RichT._

/** 
 * Since akka's is such a pain in the ass.
 * 
 * Simple actors are a type-safe DSL for asynchronous, single-threaded tasks. All threads are daemon. 
 * You probably <b>don't</b> want to compose actors, i.e., let one actor invoke another actor. Since 
 * the first actor is already running in its own thread, you can probably away with invoking 
 * whatever computation as part of its context.
 */
trait SimpleActor[Msg] {
  private val queue = Executors.newFixedThreadPool(1, new ThreadFactory() {
    override def newThread(r: Runnable) = {
      val $ = new Thread(r, s"${this.simpleName}'s actor thread")
      $.setDaemon(true)
      $
    }
  })
  protected def receive(m: Msg)
  def !(m: Msg) = queue.submit(new Runnable() {
    override def run() { receive(m) } 
  })
}