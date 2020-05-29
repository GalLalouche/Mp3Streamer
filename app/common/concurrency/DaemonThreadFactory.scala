package common.concurrency

import java.util.concurrent.ThreadFactory

import common.rich.RichT._

private object DaemonThreadFactory {
  def apply(name: String): ThreadFactory = new Thread(_, name).<|(_ setDaemon true)
}
