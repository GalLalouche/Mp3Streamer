package backend.score

sealed trait FullInfoScore {
  def toOptionalModelScore: OptionalModelScore = this match {
    case FullInfoScore.Default => OptionalModelScore.Default
    case fi: FullInfoScore.Scored => OptionalModelScore.Scored(fi.score)
  }
}
object FullInfoScore {
  case object Default extends FullInfoScore
  sealed trait Scored extends FullInfoScore {
    def score: ModelScore
    def source: ScoreSource
    def songScore: OptionalModelScore
    def albumScore: OptionalModelScore
    def artistScore: OptionalModelScore
  }

  case class SongScored(
      override val score: ModelScore,
      override val albumScore: OptionalModelScore,
      override val artistScore: OptionalModelScore,
  ) extends Scored {
    override val songScore = OptionalModelScore.Scored(score)
    override val source = ScoreSource.Song
  }
  case class AlbumScored(
      override val score: ModelScore,
      override val artistScore: OptionalModelScore,
  ) extends Scored {
    override val songScore = OptionalModelScore.Default
    override val albumScore = OptionalModelScore.Scored(score)
    override val source = ScoreSource.Album
  }
  case class ArtistScored(
      override val score: ModelScore,
  ) extends Scored {
    override val songScore = OptionalModelScore.Default
    override val albumScore = OptionalModelScore.Default
    override val artistScore = OptionalModelScore.Scored(score)
    override val source = ScoreSource.Artist
  }
}
