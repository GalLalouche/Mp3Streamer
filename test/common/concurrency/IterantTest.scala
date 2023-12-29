package common.concurrency

import java.util.concurrent.{LinkedBlockingQueue, Semaphore}
import java.util.concurrent.atomic.AtomicInteger

import org.scalatest.{AsyncFreeSpec, OneInstancePerTest}

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.BetterFutureInstances._
import common.rich.func.RichOptionT
import scalaz.OptionT

import common.rich.RichT.lazyT
import common.test.AsyncAuxSpecs

class IterantTest extends AsyncFreeSpec with AsyncAuxSpecs with OneInstancePerTest {
  implicit override def executionContext: ExecutionContext = ThreadlessContext

  private def test10($ : Iterant[Future, Int], expected: Seq[Int]) =
    $.batchStep(10).map(_._1) shouldEventuallyReturn expected
  "toStream" - {
    "empty returns empty" in {
      Iterant.empty.toStream.toStream shouldEventuallyReturn Stream.empty
    }
    "finite" in {
      Iterant.range(0, 5).toStream.toStream shouldEventuallyReturn 0.until(5).toStream
    }
    "infinite" in {
      Iterant.from(0).toStream.take(10).toStream shouldEventuallyReturn 0.until(10).toStream
    }
  }
  "filter" - {
    "empty returns empty" in {
      test10(Iterant.empty[Future, Int].filter(_ => ???), Nil)
    }
    "non-empty + trivial filter returns the same iterant" in {
      test10(
        Iterant.from(0).filter(true.const),
        0 until 10,
      )
    }
    "Proper test" in {
      test10(
        Iterant.from(0).filter(_ % 2 == 0),
        0 until 20 by 2,
      )
    }
  }

  "++" - {
    "empty ++ empty" in {
      test10(Iterant.empty[Future, Int] ++ Iterant.empty, Nil)
    }
    "finite + finite" in {
      test10(
        Iterant.range(0, 5) ++ Iterant.range(10, 15),
        0.until(5) ++ 10.until(15),
      )
    }
    "finite + infinite" in {
      test10(
        Iterant.range(0, 5) ++ Iterant.from(10),
        0.until(5) ++ 10.until(15),
      )
    }
    "infinite + infinite" in {
      test10(
        Iterant.from(0) ++ Iterant.from(100),
        0 until 10,
      )
    }
  }

  private class InterlockingIterant(semaphore: Semaphore, from: Int, to: Int = Int.MaxValue)
      extends Iterant[Future, Int] {
    override def step = {
      semaphore.acquire()
      if (from >= to)
        OptionT.none
      else
        RichOptionT.pointSome.apply(from -> new InterlockingIterant(semaphore, from + 1, to))
    }
  }

  "batchStepping with observer" - {
    val semaphore = new Semaphore(2)
    val queue = new LinkedBlockingQueue[Int]
    val actor = SimpleActor[Int](
      "foo",
      { next =>
        queue.put(next)
        semaphore.release()
      },
    )
    val onCompletedCount = new AtomicInteger(0)

    "interlocked" in {
      new InterlockingIterant(semaphore, 0)
        .batchStep(10, actor.!(_), () => ???)
        .map(_ => queue.asScala.toSeq shouldReturn 0.until(10))
    }

    "calls on completed" in {
      new InterlockingIterant(semaphore, 0, 5)
        .batchStep(10, actor.!(_), () => onCompletedCount.incrementAndGet())
        .map(_ =>
          onCompletedCount.get().shouldReturn(1) &&
            (queue.asScala.toSeq shouldReturn 0.until(5)),
        )
    }
  }
}
