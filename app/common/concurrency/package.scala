package common

package object concurrency {
  implicit def toRunnable(f: () => Unit): Runnable = new Runnable { override def run() { f() } }
}
