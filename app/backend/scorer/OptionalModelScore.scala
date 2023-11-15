package backend.scorer

import scala.collection.immutable

sealed trait OptionalModelScore {
  def toModelScore: Option[ModelScore] = this match {
    case OptionalModelScore.Default => None
    case OptionalModelScore.Scored(e) => Some(e)
  }
  def entryName: String = this match {
    case e @ OptionalModelScore.Default => e.toString
    case OptionalModelScore.Scored(score) => score.entryName
  }
}

object OptionalModelScore {
  case object Default extends OptionalModelScore
  case class Scored(score: ModelScore) extends OptionalModelScore
  def entryNames: immutable.IndexedSeq[String] =
    Default.toString +: ModelScore.values.map(_.entryName)
  def withName(name: String): OptionalModelScore =
    if (name == Default.toString) Default else Scored(ModelScore.withName(name))
  def withNameInsensitive(name: String): OptionalModelScore =
    if (name.toLowerCase == Default.toString.toLowerCase) Default
    else Scored(ModelScore.withNameInsensitive(name))
}
