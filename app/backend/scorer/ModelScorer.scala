package backend.scorer

import backend.recon.{Album, Artist}
import backend.scorer.ModelScorer.SongScore
import backend.scorer.ModelScorer.SongScore.Scored
import models.Song

import scala.concurrent.Future

private trait ModelScorer {
  def apply(s: Song): Future[SongScore]
  def updateSongScore(song: Song, score: ModelScore): Future[Unit]
  def updateAlbumScore(song: Song, score: ModelScore): Future[Unit]
  def updateArtistScore(song: Song, score: ModelScore): Future[Unit]
}

private object ModelScorer {
  sealed trait SongScore {
    def toModelScore: Option[ModelScore] = this match {
      case SongScore.Default => None
      case SongScore.Scored(score, _, _, _, _) => Some(score)
    }
  }
  object SongScore {
    case object Default extends SongScore
    case class Scored(
        score: ModelScore,
        source: Source,
        songScore: Option[ModelScore],
        albumScore: Option[ModelScore],
        artistScore: Option[ModelScore],
    ) extends SongScore {
      // TODO more ADTs!
      // TODO SoftwareDesign Could be an interesting question
      require (songScore.isDefined || albumScore.isDefined || artistScore.isDefined)
    }
  }

  sealed trait Source
  object Source {
    case object Artist extends Source
    case object Album extends Source
    case object Song extends Source
  }
}
