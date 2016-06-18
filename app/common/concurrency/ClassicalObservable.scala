package common.concurrency

import scala.collection.mutable.ListBuffer

trait ClassicalObservable[T] {
  private val observers = ListBuffer[T => Unit]()
  def +=(observer: T => Unit) {
    observers += observer
  }
  def publish(t: T) {
    observers.foreach(_(t))
  }
}
