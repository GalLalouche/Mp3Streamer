package backend.albums.gui

import backend.albums.AlbumsModel.ArtistAlbums
import backend.mb.AlbumType
import backend.recon.Artist
import backend.scorer.ModelScore
import models.Genre

import java.time.LocalDate

import monocle.macros.Lenses

@Lenses
private case class AlbumEntry(
    artist: Artist,
    albumTitle: String,
    date: LocalDate,
    genre: Option[Genre],
    score: Option[ModelScore],
    albumType: AlbumType,
)
private object AlbumEntry {
  def from(albums: ArtistAlbums): Seq[AlbumEntry] = albums.albums.map(album => AlbumEntry(
    albums.artist, album.title, album.date, albums.genre, albums.artistScore, album.albumType))
}
