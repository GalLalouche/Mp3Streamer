package common.concurrency

import java.util.concurrent.atomic.AtomicReference

import scala.concurrent.{ExecutionContext, Future}

import common.rich.RichFuture._

/**
 * Computes a value asyncly. When using get for the first time, it will block until the future has
 * finished.
 */
class AsyncVal[A >: Null](a: => A)(implicit ec: ExecutionContext) {
  private val value: AtomicReference[A] = new AtomicReference
  private val future = Future(a)
  future.foreach(value.compareAndSet(null, _))
  def get: A = Option(value.get()).getOrElse(future.get)
  def set(a: A): Unit = value.set(a)
}
