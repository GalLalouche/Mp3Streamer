package common
import java.util.concurrent.Callable

package object concurrency {
  implicit def toRunnable(f: () => Any): Runnable = new Runnable { override def run() { f() } }
  implicit def toCallable[T](t: => T): Callable[T] = new Callable[T] { override def call(): T = t }
}
