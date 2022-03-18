package backend.scorer

import backend.recon.{Artist, Album}

object OrgScoreFormatter {
  def artist(artist: Artist, score: ModelScore): String = s"ARTIST ; ${artist.name} === $score"
  def album(album: Album, score: ModelScore): String = s"ALBUM ; ${album.artistName} ;;;  ${album.title} (${album.year}) === $score"
}
