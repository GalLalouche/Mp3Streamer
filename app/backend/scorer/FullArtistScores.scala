package backend.scorer

import backend.recon.{Album, Artist}
import models.{SongTitle, TrackNumber}

import monocle.macros.Lenses

import common.io.FileRef

private object FullArtistScores {
  case class SongScore(
      file: FileRef,
      trackNumber: TrackNumber,
      title: SongTitle,
      score: OptionalModelScore,
  )
  case class AlbumScore(album: Album, score: OptionalModelScore, songScores: Seq[SongScore])
  @Lenses
  case class ArtistScore(artist: Artist, score: OptionalModelScore, albumScores: Seq[AlbumScore])
}
