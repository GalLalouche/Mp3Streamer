package common

import scala.concurrent.Future

package object concurrency {
  def toRunnable(f: () => Any): Runnable = () => f()
  type FutureIterant[A] = Iterant[Future, A]
}
