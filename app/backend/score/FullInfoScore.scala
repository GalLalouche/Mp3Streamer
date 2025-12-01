package backend.score

private sealed trait FullInfoScore {
  def toOptionalModelScore: OptionalModelScore = sourcedOptionalModelScore.toOptionalModelScore
  def sourcedOptionalModelScore: SourcedOptionalModelScore = this match {
    case FullInfoScore.Default => SourcedOptionalModelScore.Default
    case fi: FullInfoScore.Scored => SourcedOptionalModelScore.Scored(fi.score, fi.source)
  }
}
private object FullInfoScore {
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
