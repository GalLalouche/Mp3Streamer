package common.concurrency

import java.util.concurrent.LinkedBlockingQueue

import org.scalatest.{AsyncFreeSpec, Matchers}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._

import cats.implicits.toTraverseOps
import common.rich.func.kats.ToMoreFunctorOps.toMoreFunctorOps

import common.rich.RichTuple.richTuple2
import common.rich.collections.RichTraversableOnce.richTraversableOnce
import common.test.AsyncAuxSpecs

class RateLimitedActorAsyncImplTest extends AsyncFreeSpec with AsyncAuxSpecs with Matchers {
  "rate limited" in {
    val queue = new LinkedBlockingQueue[(Int, Long)]()
    val limit = 10L
    val $ = SimpleTypedActor.asyncRateLimited[Int, Unit](
      "name",
      i => Future.successful(queue.put(i, System.currentTimeMillis())),
      limit.millis,
    )
    val vector = 1.to(5).toVector
    vector.traverse($ ! _) >| {
      val v = queue.asScala.toVector
      v.map(_._1) shouldReturn vector
      all(v.map(_._2).pairSliding.map(_.swap.reduce(_ - _)).toVector) should be >= limit
    }
  }
}
