package common.concurrency.actor

import java.util.concurrent.LinkedBlockingQueue

import org.scalatest.freespec.AsyncFreeSpec

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._

import cats.implicits.toTraverseOps
import common.rich.func.kats.ToMoreFunctorOps.toMoreFunctorOps

import common.concurrency.DaemonExecutionContext
import common.rich.RichTuple.richTuple2
import common.rich.collections.RichTraversableOnce.richTraversableOnce
import common.test.AsyncAuxSpecs

class RateLimitedActorImplTest extends AsyncFreeSpec with AsyncAuxSpecs {
  private implicit val ec: ExecutionContext =
    DaemonExecutionContext("RateLimitedActorAsyncImplTest", 4)
  "rate limited" in 10.assertParTimes {
    val times = new ListBuffer[Long]()
    val queue = new LinkedBlockingQueue[Int]()
    val limit = 10L
    val $ = Actor.rateLimited[Int, Unit]("name", limit.millis) { i =>
      times += System.currentTimeMillis()
      Future(queue.put(i))
    }
    val vector = 1.to(20).toVector
    vector.traverse($ ! _) >| {
      queue.asScala.toVector.shouldMultiSetEqual(1.to(20)) // Order is not guaranteed.
      all(times.pairSliding.map(_.swap.reduce(_ - _)).toVector) should be >= limit
    }
  }
}
