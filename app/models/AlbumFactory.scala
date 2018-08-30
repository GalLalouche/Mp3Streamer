package models

import javax.inject.Inject

class AlbumFactory @Inject()(mf: MusicFinder) {
  def fromSong(s: Song): Album = Album(
    dir = s.file.parent,
    title = s.albumName,
    artistName = s.artistName,
    year = s.year,
    songs = mf getSongsInDir s.file.parent,
  )

  implicit class AlbumFactorySongOps(private val $: Song) {
    def album: Album = fromSong($)
  }
}
