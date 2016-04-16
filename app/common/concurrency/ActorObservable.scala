package common.concurrency

import rx.lang.scala._

;/** Fucking type erasure */
trait ActorObservable[T] {
  protected def apply(): Observable[T]
  private val q = new SingleThreadedJobQueue
  def !(a: SimpleActor[T]): Subscription = {
    apply().apply(
      new Observer[T] {
        override def onNext(value: T) {
          q(a ! value)
        }
        override def onError(error: Throwable): Unit = super.onError(error)
        override def onCompleted(): Unit = super.onCompleted()
      })
  }
}
