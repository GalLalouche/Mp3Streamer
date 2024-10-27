package backend.scorer.utils

import backend.recon.{Album, Artist}
import backend.recon.Reconcilable.SongExtractor
import backend.scorer.OptionalModelScore
import models.Song

private object OrgScoreFormatter {
  val PrefixSeparator = ";"
  val SectionSeparator = ";;;"
  val ScoreSeparator = "==="
  def artist(artist: Artist, score: OptionalModelScore): String =
    s"ARTIST $PrefixSeparator ${artist.name} $ScoreSeparator ${score.entryName}"
  private def albumPattern(album: Album): String = s"${album.title} (${album.year})"
  def album(album: Album, score: OptionalModelScore): String =
    s"ALBUM $PrefixSeparator ${album.artistName} $SectionSeparator ${albumPattern(album)} $ScoreSeparator ${score.entryName}"
  def song(song: Song, score: OptionalModelScore): String = {
    val album = albumPattern(song.release)
    s"SONG $PrefixSeparator ${song.artistName} $SectionSeparator $album $SectionSeparator ${song.trackNumber}. ${song.artistName} $ScoreSeparator ${score.entryName}"
  }
}
