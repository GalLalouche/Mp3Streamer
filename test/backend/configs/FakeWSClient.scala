package backend.configs

import backend.Url
import common.io.WSAliases._
import common.rich.primitives.RichBoolean._

private class FakeWSClient(getRequest: Url => WSRequest) extends WSClient {
  private var wasClosed = false
  // For printing if the client wasn't closed
  private val stackTrace = Thread.currentThread.getStackTrace drop 3

  override def underlying[T] = this.asInstanceOf[T]

  override def url(url: String): WSRequest =
    if (wasClosed) throw new IllegalStateException("WSClient is closed") else getRequest(Url(url))

  override def close(): Unit =
    if (wasClosed) throw new IllegalStateException("WSClient was already closed")
    else wasClosed = true

  override def finalize(): Unit =
    if (wasClosed.isFalse) {
      println("WSClient wasn't closed :( printing stack-trace")
      println(stackTrace mkString "\n")
      println("-----------------------------------------------")
    }
}
