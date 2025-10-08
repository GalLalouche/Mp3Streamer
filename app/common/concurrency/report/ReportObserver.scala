package common.concurrency.report

import rx.lang.scala.Observer

/** A generalization of an [[rx.lang.scala.Observer]] whose onComplete accepts a result value. */
trait ReportObserver[Agg, Result] {
  def onStep(a: Agg): Unit
  def onComplete(r: Result): Unit
  def onError(t: Throwable): Unit
}

object ReportObserver {
  def from[A, B](observer: Observer[A]) = new ReportObserver[A, B] {
    override def onStep(a: A): Unit = observer.onNext(a)
    override def onComplete(r: B): Unit = observer.onCompleted()
    override def onError(t: Throwable): Unit = observer.onError(t)
  }
}
