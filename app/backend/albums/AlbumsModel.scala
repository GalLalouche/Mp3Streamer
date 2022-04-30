package backend.albums

import backend.albums.filler.storage.FilledStorage
import backend.albums.AlbumsModel.ArtistAlbums
import backend.recon.Artist
import backend.scorer.ModelScore
import javax.inject.Inject
import models.{Genre, GenreFinder}

import scala.concurrent.{ExecutionContext, Future}

import scalaz.ListT
import common.rich.func.BetterFutureInstances._

private class AlbumsModel @Inject()(
    ec: ExecutionContext,
    storage: FilledStorage,
    genreFinder: GenreFinder,
) {
  private implicit val iec: ExecutionContext = ec

  // TODO ScalaCommon append to tuple
  def albums: ListT[Future, ArtistAlbums] = storage.all.map {case (artist, modelScore, newAlbums) =>
    ArtistAlbums(artist, modelScore, newAlbums, genreFinder.forArtist(artist))
  }

  def removeArtist(artistName: String): Future[_] = storage.remove(Artist(artistName))
  def ignoreArtist(artistName: String): Future[_] = storage.ignore(Artist(artistName))
  def removeAlbum(artist: Artist, albumName: String): Future[_] = storage.remove(artist, albumName)
  def ignoreAlbum(artist: Artist, albumName: String): Future[_] = storage.ignore(artist, albumName)
}

private object AlbumsModel {
  case class ArtistAlbums(
      artist: Artist,
      artistScore: Option[ModelScore],
      albums: Seq[NewAlbum],
      genre: Option[Genre]
  )
}