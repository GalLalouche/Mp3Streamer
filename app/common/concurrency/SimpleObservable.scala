package common.concurrency
import rx.lang.scala.Observer

import scala.collection.mutable

trait SimpleObservable[T] {
  protected val observers = mutable.ListBuffer[Observer[T]]()
  def apply(o: Observer[T]) { observers += o }
}

class Publisher[T] extends SimpleObservable[T] {
  private val worker = new SimpleActor[T] {
    override protected def apply(m: T) { observers foreach (_ onNext m) }
  }
  def publish(t: T) { worker ! t }
}

