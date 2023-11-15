package backend.scorer

sealed trait FullInfoScore {
  def toOptionalModelScore: OptionalModelScore = this match {
    case FullInfoScore.Default => OptionalModelScore.Default
    case FullInfoScore.Scored(score, _, _, _, _) => OptionalModelScore.Scored(score)
  }
}
object FullInfoScore {
  case object Default extends FullInfoScore
  case class Scored(
      score: ModelScore,
      source: ScoreSource,
      songScore: OptionalModelScore,
      albumScore: OptionalModelScore,
      artistScore: OptionalModelScore,
  ) extends FullInfoScore {
    // TODO more ADTs!
    // TODO SoftwareDesign Could be an interesting question
    require(songScore.isDefined || albumScore.isDefined || artistScore.isDefined)
  }
}
