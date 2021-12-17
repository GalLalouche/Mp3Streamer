package backend.scorer

import backend.recon.{Album, Artist}
import backend.scorer.ModelScorer.SongScore
import models.Song

import scala.concurrent.Future

private trait ModelScorer {
  def apply(s: Song): Future[SongScore]
  def updateSongScore(song: Song, score: ModelScore): Future[Unit]
  def updateAlbumScore(album: Album, score: ModelScore): Future[Unit]
  def updateArtistScore(artist: Artist, score: ModelScore): Future[Unit]
}

private object ModelScorer {
  sealed trait SongScore {
    def toModelScore: Option[ModelScore] = this match {
      case SongScore.Default => None
      case SongScore.Scored(score, _) => Some(score)
    }
  }
  object SongScore {
    case object Default extends SongScore
    case class Scored(score: ModelScore, source: Source) extends SongScore {
      require(score != ModelScore.Default)
    }
  }

  sealed trait Source {
    def apply: ModelScore => SongScore = SongScore.Scored(_, this)
  }
  object Source {
    case object Artist extends Source
    case object Album extends Source
    case object Song extends Source
  }
}
