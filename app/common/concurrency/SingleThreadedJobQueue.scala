package common.concurrency

import java.util.concurrent.Executors

import common.rich.RichT._

class SingleThreadedJobQueue { self =>
  private val name = self.simpleName
  private val queue = Executors.newFixedThreadPool(
    1, (r: Runnable) => {
      val $ = new Thread(r, s"$name's actor thread")
      $ setDaemon true
      $
    })
  def apply(a: => Unit): Unit = queue submit a
}
