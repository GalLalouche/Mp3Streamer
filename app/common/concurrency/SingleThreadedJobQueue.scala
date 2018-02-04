package common.concurrency
import java.util.concurrent.Executors

import common.rich.RichT._

class SingleThreadedJobQueue {
  self =>
  protected val name = self.simpleName
  protected val queue = Executors.newFixedThreadPool(
    1, (r: Runnable) => {
      val $ = new Thread(r, s"$name's actor thread")
      $ setDaemon true
      $
    })
  def apply(a: => Unit) {
    queue.submit(a)
  }
}
