package common

import _root_.rx.lang.scala.Observer

/** A simplified [[Observer]] without an [[Observer.onNext]]. */
trait CompletionObserver extends Observer[Nothing] {
  def onCompleted(): Unit
  def onError(e: Throwable): Unit

  override def onNext(value: Nothing): Unit = ()
}

object CompletionObserver {
  def apply(onCompleted: () => Unit, onError: Throwable => Unit) = {
    val c = onCompleted
    val e = onError
    new CompletionObserver {
      override def onCompleted(): Unit = c()
      override def onError(err: Throwable): Unit = e(err)
    }
  }
}
