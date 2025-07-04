package backend.score.file

import backend.recon.{Album, Artist, Track}
import backend.score.OptionalModelScore

private object OrgScoreFormatter {
  val PrefixSeparator = ";"
  val SectionSeparator = ";;;"
  val ScoreSeparator = "==="
  def artist(artist: Artist, score: OptionalModelScore): String =
    s"ARTIST $PrefixSeparator ${artist.name} $ScoreSeparator ${score.entryName}"
  private def albumPattern(album: Album): String = s"${album.title} (${album.year})"
  def album(album: Album, score: OptionalModelScore): String =
    s"ALBUM $PrefixSeparator ${album.artist.name} $SectionSeparator ${albumPattern(album)} $ScoreSeparator ${score.entryName}"
  def track(track: Track, score: OptionalModelScore): String = {
    val album = albumPattern(track.album)
    s"SONG $PrefixSeparator ${track.artist.name} $SectionSeparator $album $SectionSeparator ${track.album.year}. ${track.title} $ScoreSeparator ${score.entryName}"
  }
}
