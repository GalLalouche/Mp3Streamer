package backend.scorer

import enumeratum.{Enum, EnumEntry}

import scala.collection.immutable

sealed trait ModelScore extends EnumEntry

object ModelScore extends Enum[ModelScore] {
  case object Crappy extends ModelScore // Why is this even still in your playlist?
  case object Meh extends ModelScore
  case object Okay extends ModelScore
  case object Good extends ModelScore
  case object Great extends ModelScore
  case object Amazing extends ModelScore // Really, really good songs you haven't gotten sick of yet
  case object Classic extends ModelScore // The Free Stairway to Bohemian Sultans

  override def values: immutable.IndexedSeq[ModelScore] = findValues
  implicit class RichModelScore(private val $: Option[ModelScore]) extends AnyVal {
    def orDefaultString: String = $.fold("Default")(_.toString)
  }
}
