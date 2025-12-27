package common.concurrency

import java.util
import java.util.concurrent.Semaphore

import scala.collection.mutable
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration

import cats.data.OptionT
import cats.implicits.{toFunctorOps, toTraverseOps}

import common.concurrency.ParallelIterantMapper.{BlockingMap, MaxReached}
import common.rich.RichT.richT

/**
 * Handles both prefetching (up to `buffer`) and parallel mapping (using instance-local
 * `parallelism` threads) of an [[Iterant]].
 */
private class ParallelIterantMapper[A, B] private (
    _iterant: FutureIterant[A],
    curriedF: ExplicitRetriever[A, B],
    buffer: Int,
    parallelism: Int,
)(implicit
    ec: ExecutionContext, // The implicit is needed for the Monad instance.
) {
  // The basic algorithm goes like this:
  // - The actual API exposed here is the iterant, which just calls get on successive indices,
  //   taking its values from the vector.
  // - An actor is started which will sequentially step through the source iterant, feeding it to a
  //   blocking map which has capacity `buffer`.
  // - When get is called, if the results vector does not yet have the requested index, we fill it
  //   up to required index by calling blockingMap.get, which will block waiting for the actor to
  //   put new elements into it.
  // - If the actor reaches the end of the iterant, it sets the max index of the blocking map,
  //   causing any further get calls beyond that index to throw an exception and interrupt any
  //   waiting threads.
  private[this] val f =
    curriedF.withExecutionContext(DaemonExecutionContext("ParallelIterantMapper", parallelism))
  private[this] val blockingMap = new BlockingMap[B](buffer)
  // Must be synchronized when read and written, since different threads can call get.
  private[this] val results = new util.Vector[Future[B]]
  private[this] def get(i: Int): Option[Future[B]] = {
    if (i > blockingMap.maxIndex) // This one isn't strictly necessary for correctness.
      return None
    // If the blocking map has not yet produced this index, we fill it up to the given index.
    if (results.size <= i)
      results.synchronized {
        while (results.size <= i)
          try
            results.add(blockingMap.get(i))
          catch {
            case MaxReached => return None
          }
      }
    Some(results.get(i))
  }

  private[this] val blockingMapFiller = SimpleActor.withSelf[FutureIterant[A]](
    "ParallelIterantMapper",
    (iterant, self) =>
      // We block here to avoid taking up a thread for foreach. This will just block up the current
      // actor thread, which is what we want.
      Await.result(iterant.step.value, Duration.Inf) match {
        case None => blockingMap.setMaxIndex()
        case Some((head, tail)) =>
          // While fetching the next value is done sequentially, mapping is done in parallel up to the
          // capacity of the blocking map and the parallelism parameter.
          blockingMap.put(f(head))
          self ! tail
      },
  )

  private[this] class Stepper(index: Int) extends FutureIterant[B] {
    override def step: Step[B] = OptionT(get(index).sequence).tupleRight(new Stepper(index + 1))
  }
  private def start(): Unit = blockingMapFiller ! _iterant
  private def iterant: Iterant[Future, B] = new Stepper(0)
}

private object ParallelIterantMapper {
  def apply[A, B](
      iterant: FutureIterant[A],
      f: ExplicitRetriever[A, B],
      n: Int,
      parallelism: Int,
  )(implicit
      ec: ExecutionContext,
  ): FutureIterant[B] = new ParallelIterantMapper(iterant, f, n, parallelism).<|(_.start()).iterant

  private object MaxReached extends RuntimeException

  /**
   * A map variant of a blocking queue. `get` blocks until `put` is called for the given key. Like a
   * queue, this get/put is a one time operation: after a get, the key/value is removed from the
   * map. And also like a queue, `put` will block if the maximal capacity has been reached.
   */
  private class BlockingMap[A](maxCapacity: Int) {
    private val futures = new mutable.HashMap[Int, Future[A]]()
    private val waiting = new mutable.HashMap[Int, SingleLatch]()
    private var _maxIndex: Int = Int.MaxValue
    @inline def maxIndex: Int = synchronized(_maxIndex)
    private var nextIndex = 0
    private val semaphore = new Semaphore(maxCapacity)
    def put(v: => Future[A]): Unit = {
      def checkMaxIndex(): Unit =
        if (maxIndex <= nextIndex)
          throw new IllegalStateException(
            s"Cannot put more elements; max index of $maxIndex has been reached",
          )

      synchronized {
        checkMaxIndex()
      }
      semaphore.acquire()
      synchronized {
        checkMaxIndex()
        futures.put(nextIndex, v)
        waiting.getOrElseUpdate(nextIndex, SingleLatch()).release()
        nextIndex += 1
      }
    }
    def get(k: Int): Future[A] = {
      val latch = synchronized {
        if (k >= maxIndex)
          throw MaxReached
        waiting.getOrElseUpdate(k, SingleLatch())
      }
      latch.await()
      synchronized {
        waiting.remove(k).ensuring(_.isDefined)
        semaphore.release()
        futures.remove(k).get
      }
    }
    def setMaxIndex(): Unit = synchronized {
      assert(_maxIndex == Int.MaxValue, "Max index can only be set once.")
      _maxIndex = nextIndex
      val keysToRemove = waiting.keySet.view.filter(_ > maxIndex).toVector
      keysToRemove.foreach(waiting.remove(_).get.interrupt(MaxReached))
      semaphore.drainPermits()
      semaphore.release(Int.MaxValue - maxCapacity) // Release all waiting puts.
    }
  }
}
