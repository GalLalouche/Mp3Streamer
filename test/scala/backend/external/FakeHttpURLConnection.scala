package backend.external

import java.net.HttpURLConnection

class FakeHttpURLConnection(real: HttpURLConnection) extends HttpURLConnection(real.getURL) {
  override def disconnect(): Unit = throw new AssertionError()
  override def usingProxy(): Boolean = throw new AssertionError()
  override def connect(): Unit = throw new AssertionError()
}
