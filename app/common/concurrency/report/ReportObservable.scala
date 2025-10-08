package common.concurrency.report

import rx.lang.scala.{Observable, Observer, Subscription}

import common.rich.func.MoreObservableInstances._
import scalaz.syntax.functor.ToFunctorOps

import common.rich.primitives.RichBoolean.richBoolean

object ReportObservable {
  type ReportObservable[Agg, Result] = Function[ReportObserver[Agg, Result], Subscription]
  def aggregator[Agg, Result](
      observable: Observable[Agg],
      finisher: Seq[Agg] => Result,
  ): ReportObservable[Agg, Result] = filteringAggregator(observable.strengthL(true), finisher)

  type ShouldNotify = Boolean
  def filteringAggregator[Agg, Result](
      observable: Observable[(ShouldNotify, Agg)],
      finisher: Seq[Agg] => Result,
  ): ReportObservable[Agg, Result] = apply(
    Observable[Report[Agg, Result]](s =>
      observable
        .doOnError(s.onError)
        .foldLeft(Vector.newBuilder[Agg]) { case (buffer, (shouldNotify, next)) =>
          if (shouldNotify)
            s.onNext(Aggregation(next))
          buffer += next
        }
        .map(_.result())
        .subscribe(buffer => s.onNext(Result(finisher(buffer)))),
    ),
  )
  def apply[Agg, Result](o: Observable[Report[Agg, Result]]): ReportObservable[Agg, Result] = ro =>
    o.subscribe(new Observer[Report[Agg, Result]] {
      private var hasCompleted = false
      private var hasErrored = false
      override def onNext(value: Report[Agg, Result]) = synchronized {
        value match {
          case Aggregation(value) =>
            require(hasCompleted.isFalse)
            require(hasErrored.isFalse)
            ro.onStep(value)
          case Result(value) =>
            require(hasCompleted.isFalse)
            require(hasErrored.isFalse)
            hasCompleted = true
            ro.onComplete(value)
        }
      }
      override def onError(error: Throwable) = synchronized {
        require(hasCompleted.isFalse)
        require(hasErrored.isFalse)
        hasErrored = true
        ro.onError(error)
      }
      override def onCompleted() = {
        require(hasCompleted)
        require(hasErrored.isFalse)
      }
    })
}
