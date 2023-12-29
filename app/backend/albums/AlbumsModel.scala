package backend.albums

import java.time.Duration
import javax.inject.Inject

import backend.albums.AlbumsModel.{ArtistAlbums, ModelResult, NonIgnoredArtist}
import backend.albums.filler.NewAlbumFiller
import backend.albums.filler.storage.FilledStorage
import backend.mb.AlbumType
import backend.recon.{Artist, IgnoredReconResult}
import backend.scorer.OptionalModelScore
import models.{Genre, GenreFinder}
import shapeless.syntax.std.tuple.productTupleOps

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.BetterFutureInstances._
import scalaz.ListT
import scalaz.Scalaz.{ToBindOps, ToFunctorOpsUnapply}

import common.rich.RichEnumeratum.richEnumeratum
import common.rich.RichT.richT

private class AlbumsModel @Inject() (
    ec: ExecutionContext,
    storage: FilledStorage,
    genreFinder: GenreFinder,
    filler: NewAlbumFiller,
) {
  private implicit val iec: ExecutionContext = ec

  def albums: ListT[Future, ModelResult] =
    storage.all.map(e => e :+ genreFinder.forArtist(e.artist) |> Function.tupled(ModelResult.apply))
  private def albumsForArtist(artist: Artist) =
    filler.update(Duration.ofDays(90), 10)(artist) >>
      storage
        .forArtist(artist)
        .map(_.sortBy(a => (AlbumType.ordinal(a.albumType), -a.date.toEpochDay)))
  def forArtist(artistName: String): Future[ArtistAlbums] = {
    val artist = Artist(artistName).normalized
    storage
      .isIgnored(artist)
      .flatMap {
        case IgnoredReconResult.Ignored => Future.successful(true)
        case IgnoredReconResult.NotIgnored => Future.successful(false)
        case IgnoredReconResult.Missing => storage.newArtist(artist).>|(false)
      }
      .ifM(
        Future.successful(AlbumsModel.IgnoredArtist),
        albumsForArtist(artist).map(NonIgnoredArtist),
      )
  }

  def removeArtist(artistName: String): Future[_] = storage.remove(Artist(artistName))
  def ignoreArtist(artistName: String): Future[_] = storage.ignore(Artist(artistName))
  def unignoreArtist(artistName: String): Future[Seq[NewAlbum]] =
    storage.unignore(Artist(artistName)) >> albumsForArtist(Artist(artistName).normalized)
  def removeAlbum(artist: Artist, albumName: String): Future[_] = storage.remove(artist, albumName)
  def ignoreAlbum(artist: Artist, albumName: String): Future[_] = storage.ignore(artist, albumName)
}

private object AlbumsModel {
  case class ModelResult(
      artist: Artist,
      artistScore: OptionalModelScore,
      albums: Seq[NewAlbum],
      genre: Option[Genre],
  )
  sealed trait ArtistAlbums
  case class NonIgnoredArtist(albums: Seq[NewAlbum]) extends ArtistAlbums
  case object IgnoredArtist extends ArtistAlbums
}
