package backend.scorer

sealed trait FullInfoScore {
  def toModelScore: Option[ModelScore] = this match {
    case FullInfoScore.Default => None
    case FullInfoScore.Scored(score, _, _, _, _) => Some(score)
  }
}
object FullInfoScore {
  case object Default extends FullInfoScore
  case class Scored(
      score: ModelScore,
      source: ScoreSource,
      songScore: Option[ModelScore],
      albumScore: Option[ModelScore],
      artistScore: Option[ModelScore],
  ) extends FullInfoScore {
    // TODO more ADTs!
    // TODO SoftwareDesign Could be an interesting question
    require(songScore.isDefined || albumScore.isDefined || artistScore.isDefined)
  }
}
