package common.concurrency

import java.util.concurrent.LinkedBlockingQueue

import org.scalatest.{AsyncFreeSpec, Matchers}

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.concurrent.duration._

import scalaz.std.scalaFuture.futureInstance
import scalaz.std.vector.vectorInstance
import scalaz.syntax.functor.ToFunctorOps
import scalaz.syntax.std.tuple.ToTuple2Ops
import scalaz.syntax.traverse.ToTraverseOps

import common.rich.collections.RichSeq._
import common.test.AsyncAuxSpecs

class RateLimitedActorAsyncImplTest extends AsyncFreeSpec with AsyncAuxSpecs with Matchers {
  "rate limited" in {
    val queue = new LinkedBlockingQueue[(Int, Long)]()
    val $ = SimpleTypedActor.asyncRateLimited[Int, Unit](
      "name",
      i => Future.successful(queue.put(i, System.currentTimeMillis())),
      10.millis,
    )
    1.to(5).toVector.traverse($ ! _) >| {
      val v = queue.asScala.toVector
      v.map(_._1) shouldReturn 1.to(5).toVector
      all(v.map(_._2).pairSliding.map(_.swap.fold(_ - _)).toVector) should be >= 10L
    }
  }
}
