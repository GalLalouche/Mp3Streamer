package backend.albums

import backend.albums.filler.storage.FilledStorage
import backend.albums.AlbumsModel.ArtistAlbums
import backend.logging.Logger
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
    logger: Logger,
) {
  private implicit val iec: ExecutionContext = ec

  // For debugging
  private[albums] def albumsRaw: ListT[Future, ArtistAlbums] = storage.allRaw.map {case (artist, modelScore, newAlbums) =>
    ArtistAlbums(artist, modelScore, newAlbums, genreFinder.forArtist(artist))
  }
  def albumsFiltered: ListT[Future, ArtistAlbums] = storage.allFiltered.map {case (artist, modelScore, newAlbums) =>
    ArtistAlbums(artist, modelScore, newAlbums, genreFinder.forArtist(artist))
  }

  def removeArtist(artistName: String): Future[_] = {
    logger.debug(s"Removing artist <$artistName>")
    storage.remove(Artist(artistName))
  }
  def ignoreArtist(artistName: String): Future[_] = {
    logger.debug(s"Ignoring artist <$artistName>")
    storage.ignore(Artist(artistName))
  }
  def removeAlbum(artist: Artist, albumName: String): Future[_] = {
    logger.debug(s"Removing album <${artist.name} - $albumName>")
    storage.remove(artist, albumName)
  }
  def ignoreAlbum(artist: Artist, albumName: String): Future[_] = {
    logger.debug(s"Ignoring album <${artist.name} - $albumName>")
    storage.ignore(artist, albumName)
  }
}

private object AlbumsModel {
  case class ArtistAlbums(
      artist: Artist,
      artistScore: Option[ModelScore],
      albums: Seq[NewAlbum],
      genre: Option[Genre]
  )
}