package backend.scorer.utils

import backend.scorer.{ModelScore, OptionalModelScore}
import backend.scorer.utils.OrgScoreFormatter.{PrefixSeparator, ScoreSeparator}

import scala.{util => su}
import scala.util.{Failure, Success, Try}

import common.rich.RichEnumeratum.richEnumeratum
import common.rich.collections.RichTraversableOnce.richTraversableOnce
import common.rich.primitives.RichOption.richOption
import common.rich.primitives.RichString._

private trait ScoreParserTemplate[A] {
  def apply(line: String): Try[(A, Option[ModelScore])] = toTry(line)

  protected def prefix: String
  protected def entity(s: Seq[String]): Try[A]

  private def toTry(s: String): su.Try[(A, Option[ModelScore])] = {
    val split = s.split(s" $ScoreSeparator ")
    if (split.length != 2)
      return su.Failure(new Exception(s"Expected a string of the format 'A === B', got '$s'"))
    for {
      e <- prepare(split(0)).flatMap(entity)
      s <- ModelScore
        .withPrefixCaseInsensitive(split(1))
        .singleOpt
        .toTry(new Exception(s"Invalid on ambiguous score '${split(1)}'"))
        .map(_.toString)
        .map(OptionalModelScore.withName)
    } yield (e, s.toModelScore)
  }

  private val prefixOrg = "\\**".r.pattern
  private def prepare(s: String): Try[Seq[String]] = {
    val stripped = s.trim.removeAll(prefixOrg).trim
    val p = s"$prefix $PrefixSeparator"
    if (stripped.startsWith(p))
      Success(stripped.drop(p.length).split(OrgScoreFormatter.SectionSeparator).map(_.trim))
    else
      Failure(new Exception(s"Line '$stripped' did not begin with '$p'"))
  }
}
