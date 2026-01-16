package common.concurrency

import java.util.concurrent.CountDownLatch

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.tagobjects.Slow

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

import common.rich.primitives.RichInt.Rich
import common.test.AuxSpecs

class ElasticExecutorTest extends AnyFreeSpec with AuxSpecs {
  "Will create new threads on long running tasks, but kill them on timeout" in {
    val bound = 3
    val keepAlive = 20.millis
    val $ = ElasticExecutor("test", daemon = true, keepAlive = keepAlive, bound = bound)
    val n = 20
    val cdl = new CountDownLatch(n)
    n.times {
      $.execute { () =>
        Thread.sleep(1)
        cdl.countDown()
      }
    }

    cdl.await()
    $.getLargestPoolSize shouldReturn bound
    Thread.sleep(keepAlive.toMillis * 3)
    $.getPoolSize shouldReturn 0
  }

  "Burst threads are removed" in {
    val bound = 3
    val keepAlive = 20.millis
    val $ = ElasticExecutor("test", daemon = true, keepAlive = keepAlive, bound = bound)
    val cdl = new CountDownLatch(bound)
    bound.times {
      $.execute { () =>
        Thread.sleep(keepAlive.toMillis / 3)
        cdl.countDown()
      }
    }
    cdl.await()
    keepAlive.toMillis.toInt.times {
      val internal = SingleLatch()
      $.execute { () =>
        internal.release()
      }
      internal.await()
      Thread.sleep(2)
    }

    $.getLargestPoolSize shouldReturn bound
    $.getPoolSize shouldReturn 1
  }

  implicit val executionContext: ExecutionContext = DaemonExecutionContext("ElasticExecutorTest", 8)
  "Elastic thread creation (No creation if there are idle ones)" taggedAs Slow in 10.parTimes {
    val bound = 3
    val keepAlive = 400.millis
    val $ = ElasticExecutor("test", daemon = true, keepAlive = keepAlive, bound = bound)
    val n = 5
    val cdl = new CountDownLatch(n)
    n.times {
      val localCdl = SingleLatch()
      $.execute { () =>
        Thread.sleep(1)
        cdl.countDown()
        localCdl.await()
      }
      localCdl.release()
      Thread.sleep(200)
    }

    cdl.await()
    $.getLargestPoolSize shouldReturn 1
    Thread.sleep(keepAlive.toMillis * 3)
    $.getPoolSize shouldReturn 0
  }
}
