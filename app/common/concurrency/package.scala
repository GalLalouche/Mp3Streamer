package common
import java.util.concurrent.Callable

package object concurrency {
  implicit def toRunnable(f: () => Any): Runnable = () => f()
  implicit def toCallable[T](t: => T): Callable[T] = () => t
}
