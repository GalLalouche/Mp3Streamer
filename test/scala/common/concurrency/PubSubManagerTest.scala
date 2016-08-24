package common.concurrency

import org.scalatest.{FreeSpec, OneInstancePerTest}

class PubSubManagerTest extends FreeSpec with OneInstancePerTest {
  val $ = new PubSubManager
  val interceptor = new MessageInterceptor[String]
  "subbing and pubbing" in {
    $ sub interceptor.intercept
    $ pub "Hello"
    interceptor expectMessage "Hello"
  }

  "unsubbing" in {
    val f = interceptor.intercept _
    $ sub f
    $ unsub f
    $ pub "Hi"
    interceptor.expectNone()
  }
}
