package mains.random_folder

import backend.recon.Artist
import genre.Genre
import org.typelevel.ci.CIString

private case class SongData(genre: Genre, artist: Artist, album: CIString)
