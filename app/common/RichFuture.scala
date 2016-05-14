package common

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

object RichFuture {
  implicit class richFuture[T]($: Future[T])(implicit ec: ExecutionContext) {
    def filterWithMessage(p: T => Boolean, message: String): Future[T] = filterWithMessage(p, e => message)
    def filterWithMessage(p: T => Boolean, message: T => String): Future[T] = $.flatMap(e => {
      if (p(e))
        $
      else
        Future.failed(new NoSuchElementException(message(e)))
    })
    def get: T = Await.result($, Duration.Inf)
    def orElse(t: => T) = $.recover {case e => t}
    def orElseTry(t: => Future[T]) = $.recoverWith {case e => t}
  }
}
