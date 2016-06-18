package common.concurrency

import java.lang.Object
import java.util.Objects
import java.util.concurrent.ConcurrentHashMap

import scala.collection.JavaConversions._

class PubSubManager {
  private val map = new ConcurrentHashMap[Manifest[_], List[_ => Unit]]().withDefault(e => List())
  private val worker = new SingleThreadedJobQueue
  def sub[T](f: T => Unit)(implicit m: Manifest[T]) {
    map put(m, f :: map(m))
  }
  def unsub[T](f: T => Unit)(implicit m: Manifest[T]) {
    map put(m, map(m) filterNot (_ eq f))
  }
  def pub[T](t: T)(implicit m: Manifest[T]) {
    worker apply map(m).foreach(_.asInstanceOf[T => Unit](t))
  }
}
