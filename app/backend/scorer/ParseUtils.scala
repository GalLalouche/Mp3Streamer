package backend.scorer

import scala.{util => su}
import scala.util.parsing.combinator.RegexParsers

import common.rich.RichEnumeratum.richEnumeratum
import common.rich.collections.RichTraversableOnce.richTraversableOnce

private trait ParseUtils[A] extends RegexParsers {
  protected val score = ".*".r ^^ (ModelScore.withPrefixCaseInsensitive(_).single)
  protected def main: Parser[A]
  def toTry(s: String): su.Try[A] = parseAll(main, s) match {
    case Success(e, _) => su.Success(e)
    case Failure(msg, _) => su.Failure(new Exception(msg))
    case Error(msg, _) => throw new Exception(msg)
  }
}
