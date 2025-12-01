package backend.score

sealed trait SourcedOptionalModelScore {
  def isDefined: Boolean = this match {
    case SourcedOptionalModelScore.Default => false
    case _: SourcedOptionalModelScore.Scored => true
  }
  def toOptionalModelScore: OptionalModelScore = this match {
    case SourcedOptionalModelScore.Default => OptionalModelScore.Default
    case SourcedOptionalModelScore.Scored(e, _) => OptionalModelScore.Scored(e)
  }
  def toModelScore: Option[ModelScore] = toOptionalModelScore.toModelScore

  def source: Option[ScoreSource] = this match {
    case SourcedOptionalModelScore.Default => None
    case SourcedOptionalModelScore.Scored(_, source) => Some(source)
  }
}

object SourcedOptionalModelScore {
  case object Default extends SourcedOptionalModelScore
  case class Scored(score: ModelScore, _source: ScoreSource) extends SourcedOptionalModelScore
}
