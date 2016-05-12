package common

import scala.util.{Failure, Success, Try}

object RichTry {
  implicit class richTry[T]($: Try[T]) {
    def filterWithMessage(p: T => Boolean, message: String): Try[T] = $ match {
      case Success(e) => if (p(e)) $ else Failure(new NoSuchElementException(message))
      case _ => $
    }
  }
}
