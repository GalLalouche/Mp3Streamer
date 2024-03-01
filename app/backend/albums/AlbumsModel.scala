package backend.albums

import java.time.Duration
import javax.inject.Inject

import backend.albums.AlbumsModel.{ArtistAlbums, ModelResult, NonIgnoredArtist}
import backend.albums.filler.NewAlbumFiller
import backend.albums.filler.storage.FilledStorage
import backend.mb.AlbumType
import backend.recon.{Artist, IgnoredReconResult}
import backend.scorer.OptionalModelScore
import models.{AlbumTitle, Genre, GenreFinder}
import models.TypeAliases.ArtistName
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
  def forArtist(name: ArtistName): Future[ArtistAlbums] = {
    val artist = Artist(name).normalized
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

  def removeArtist(name: ArtistName): Future[_] = storage.remove(Artist(name))
  def ignoreArtist(name: ArtistName): Future[_] = storage.ignore(Artist(name))
  def unignoreArtist(name: ArtistName): Future[Seq[NewAlbum]] =
    storage.unignore(Artist(name)) >> albumsForArtist(Artist(name).normalized)
  def removeAlbum(artist: Artist, title: AlbumTitle): Future[_] = storage.remove(artist, title)
  def ignoreAlbum(artist: Artist, title: AlbumTitle): Future[_] = storage.ignore(artist, title)
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
