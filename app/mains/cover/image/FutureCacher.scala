package mains.cover.image

import java.util.concurrent.LinkedBlockingQueue

import scala.collection.AbstractIterator
import scala.concurrent.{ExecutionContext, Future}

import common.rich.primitives.RichBoolean.richBoolean

// TODO move to somewhere common
private object FutureCacher {
  /** Returned cache is *not* thread-safe!. */
  def apply[A](f: Iterator[Future[Seq[A]]])(implicit ec: ExecutionContext): Iterator[Future[A]] = {
    new AbstractIterator[Future[A]] {
      private val cache = new LinkedBlockingQueue[A]()
      override def hasNext = cache.isEmpty.isFalse || f.hasNext
      override def next() =
        if (cache.isEmpty)
          f.next().flatMap {xs =>
            xs.foreach(cache.put)
            next()
          }
        else
          Future.successful(cache.poll())
    }
  }
}
