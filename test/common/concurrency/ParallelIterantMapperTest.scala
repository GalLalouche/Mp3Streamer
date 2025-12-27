package common.concurrency

import java.util
import java.util.concurrent.atomic.AtomicInteger

import org.scalatest.OneInstancePerTest
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.tags.Slow

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

import common.rich.func.kats.ToMoreFunctorOps.toMoreFunctorOps

import common.test.AsyncAuxSpecs

@Slow
class ParallelIterantMapperTest extends AsyncFreeSpec with AsyncAuxSpecs with OneInstancePerTest {
  private val Parallelization = 16
  implicit override val executionContext: ExecutionContext =
    DaemonExecutionContext("ParallelIterantMapperTest", 8)
  private val int = new AtomicInteger(0)
  private def sourceIterant: FutureIterant[Int] = Iterant.forever[Future, Int] {
    val idx = int.getAndIncrement()
    Future {
      Thread.sleep(Random.nextInt(10))
      idx
    }
  }

  private def mapper(f: Int => Int): ExplicitRetriever[Int, Int] = ec => { i =>
    Future {
      Thread.sleep(Random.nextInt(100))
      f(i)
    }(ec)
  }

  private def iterant(i: FutureIterant[Int], f: Int => Int) =
    Iterant.parallelPrefetching(i, mapper(f), prefetchSize = 10, parallelism = Parallelization)

  "basic infinite test (sync)" in {
    val iter = iterant(sourceIterant, _ * 2)

    checkAll(
      takeTest(iter, 10, 10),
      takeTest(iter, 20, 20),
      takeTest(iter, 30, 30),
    ) >| (int.get() should be <= 41)
  }
  "basic infinite test (async)" in {
    val iter = iterant(sourceIterant, _ * 2)

    checkAllParallel(
      takeTest(iter, 10, 10),
      takeTest(iter, 20, 20),
      takeTest(iter, 30, 30),
    ) >| (int.get() should be <= 41)
  }

  "basic finite test (sync)" in 5.parTimes {
    val iter = iterant(sourceIterant.take(20), _ * 2)
    checkAll(
      takeTest(iter, 10, 10),
      takeTest(iter, 20, 20),
      takeTest(iter, 30, 20),
      takeTest(iter, 40, 20),
    ) >| (int.getAndSet(0) shouldReturn 20)
  }
  "basic finite test (async)" in 5.parTimes {
    val iter = iterant(sourceIterant.take(20), _ * 2)

    checkAllParallel(
      takeTest(iter, 10, 10),
      takeTest(iter, 20, 20),
      takeTest(iter, 30, 20),
      takeTest(iter, 40, 20),
    ) >| (int.getAndSet(0) shouldReturn 20)
  }

  "data is fetched in parallel" in {
    val set = util.Collections.synchronizedSet(new util.HashSet[Long]())
    val iter = iterant(
      sourceIterant.take(64),
      i => {
        set.add(Thread.currentThread().getId)
        i * 2
      },
    )

    // Can't be sure how many actual threads this will end up using since it uses an elastic pool.
    takeTest(iter, 32, 32) >| { set.size() should be > 1 }
  }

  private def takeTest(iter: FutureIterant[Int], n: Int, maxInt: Int) =
    iter.take(n).toSeq shouldEventuallyReturn 0.until(maxInt).map(_ * 2).toVector
}
