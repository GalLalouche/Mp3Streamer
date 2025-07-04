package backend.score

import enumeratum.{Enum, EnumEntry}

import scala.collection.immutable

import common.rich.func.ToMoreFoldableOps.toMoreFoldableOps
import scalaz.std.option.optionInstance

sealed trait ModelScore extends EnumEntry

object ModelScore extends Enum[ModelScore] {
  case object Crappy extends ModelScore // Why is this even still in your playlist?
  case object Meh extends ModelScore
  case object Okay extends ModelScore
  case object Good extends ModelScore
  case object Great extends ModelScore
  case object Amazing extends ModelScore // Really, really good songs you haven't gotten sick of yet

  override def values: immutable.IndexedSeq[ModelScore] = findValues
  implicit class RichModelScore(private val $ : Option[ModelScore]) extends AnyVal {
    def orDefaultString: String = toOptionalModelScore.entryName
    def toOptionalModelScore: OptionalModelScore =
      $.mapHeadOrElse(OptionalModelScore.Scored, OptionalModelScore.Default)
  }
}
