package common

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

object RichFuture {
  implicit class richFuture[T]($: Future[T])(implicit ec: ExecutionContext) {
    // gives better error message when the filter fails
    def filterWith(p: T => Boolean, message: String): Future[T] = filterWithMessage(p, e => message)
    // implicits suck with overloads it seems
    def filterWithMessage(p: T => Boolean, message: T => String): Future[T] = $.flatMap(e => {
      if (p(e))
        $
      else
        Future failed new NoSuchElementException(message(e))
    })
    def get: T = Await.result($, Duration.Inf)
    // like recover, but doesn't care about the failure
    def orElse(t: => T): Future[T] = $.recover { case e => t }
    // implicits suck with overloads it seems
    def orElseTry(t: => Future[T]): Future[T] = $.recoverWith { case e => t }
    def onEnd[S](f: => Future[S]): Future[S] = $.flatMap(e => f)
  }
  implicit class richOptionFuture[T]($: Future[Option[T]])(implicit ec: ExecutionContext) {
    def ifNone(t: => T): Future[T] = $.map(_ getOrElse t)
    def ifNoneTry(t: => Future[T]): Future[T] = $.flatMap(_ map Future.successful getOrElse t)
  }
}
