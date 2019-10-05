package common

package object concurrency {
  def toRunnable(f: () => Any): Runnable = () => f()
}
