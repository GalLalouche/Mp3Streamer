package backend.score.file

import backend.score.{ModelScore, OptionalModelScore}
import backend.score.file.OrgScoreFormatter.{PrefixSeparator, ScoreSeparator}
import backend.score.file.ScoreParserTemplate.isDefault

import scala.{util => su}
import scala.util.{Failure, Success, Try}

import common.rich.func.kats.ToMoreApplyOps.toMoreApplyOps

import common.rich.RichEnumeratum.richEnumeratum
import common.rich.collections.RichTraversableOnce.richTraversableOnce
import common.rich.primitives.RichOption.richOption
import common.rich.primitives.RichString._

private trait ScoreParserTemplate[A] {
  def apply(line: String): Try[(A, OptionalModelScore)] = toTry(line)

  protected def prefix: String
  protected def entity(s: Vector[String]): Try[A]
  private def toTry(s: String): su.Try[(A, OptionalModelScore)] = {
    val split = s.split(s" $ScoreSeparator ")
    if (split.length != 2)
      return su.Failure(new Exception(s"Expected a string of the format 'A === B', got '$s'"))

    val e = prepare(split(0)).flatMap(entity)
    val score =
      if (isDefault(split(1)))
        Success(OptionalModelScore.Default)
      else
        ModelScore
          .withPrefixCaseInsensitive(split(1))
          .singleOpt
          .toTry(new Exception(s"Invalid on ambiguous score '${split(1)}'"))
          .map(_.toString)
          .map(OptionalModelScore.withName)
    e.tuple(score)
  }

  private val prefixOrg = "\\**".r.pattern
  private def prepare(s: String): Try[Vector[String]] = {
    val stripped = s.trim.removeAll(prefixOrg).trim
    val p = s"$prefix $PrefixSeparator"
    if (stripped.startsWith(p))
      Success(
        stripped.drop(p.length).split(OrgScoreFormatter.SectionSeparator).map(_.trim).toVector,
      )
    else
      Failure(new Exception(s"Line '$stripped' did not begin with '$p'"))
  }
}

private object ScoreParserTemplate {
  // Necessary for the below implementation of isDefault to be correct.
  assert(ModelScore.values.fornone(_.entryName.head.toLower == 'd'))
  def isDefault(s: String): Boolean = s.headOption.exists(_.toLower == 'd')
}
