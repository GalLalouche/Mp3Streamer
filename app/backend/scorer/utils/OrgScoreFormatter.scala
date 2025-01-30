package backend.scorer.utils

import backend.recon.{Album, Artist}
import backend.scorer.ModelScore
import backend.scorer.ModelScore._

private object OrgScoreFormatter {
  val PrefixSeparator = ";"
  val SectionSeparator = ";;;"
  val ScoreSeparator = "==="
  def artist(artist: Artist, score: Option[ModelScore]): String =
    s"ARTIST $PrefixSeparator ${artist.name} $ScoreSeparator ${score.orDefaultString}"
  def album(album: Album, score: Option[ModelScore]): String =
    s"ALBUM $PrefixSeparator ${album.artist.name} $SectionSeparator  ${album.title} (${album.year}) $ScoreSeparator ${score.orDefaultString}"
}
