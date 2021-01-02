package common.concurrency.report

/** Utility ADT for conversion from [[rx.lang.scala.Observable]] to [[ReportObservable]]. */
sealed trait Report[+Agg, +Result]
case class Aggregation[Agg](value: Agg) extends Report[Agg, Nothing]
case class Result[R](value: R) extends Report[Nothing, R]
