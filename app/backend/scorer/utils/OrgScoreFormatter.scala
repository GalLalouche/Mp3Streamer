package backend.scorer.utils

import backend.recon.{Album, Artist}
import backend.scorer.ModelScore
import backend.scorer.ModelScore._

private object OrgScoreFormatter {
  def artist(artist: Artist, score: Option[ModelScore]): String =
    s"ARTIST ; ${artist.name} === ${score.orDefaultString}"
  def album(album: Album, score: Option[ModelScore]): String =
    s"ALBUM ; ${album.artistName} ;;;  ${album.title} (${album.year}) === ${score.orDefaultString}"
}
