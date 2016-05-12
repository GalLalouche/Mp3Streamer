package common

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

object RichFuture {
  implicit class richFuture[T]($: Future[T]) {
    def filterWithMessage(p: T => Boolean, message: String)(implicit ec: ExecutionContext): Future[T] = filterWithMessage(p, e => message)(ec)
    def filterWithMessage(p: T => Boolean, message: T => String)(implicit ec: ExecutionContext): Future[T] = $.flatMap(e => {
      if (p(e))
        $
      else
        Future.failed(new NoSuchElementException(message(e)))
    })
    def get: T = Await.result($, Duration.Inf)
    def orElse(t: => T)(implicit ec: ExecutionContext) = $.recover{case e => t}
  }
}
