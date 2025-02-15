package backend.new_albums

import java.time.Duration
import javax.inject.Inject

import backend.mb.AlbumType
import backend.new_albums.NewAlbumsModel.{ArtistAlbums, ModelResult, NonIgnoredArtist, Unreconciled}
import backend.new_albums.filler.NewAlbumFiller
import backend.new_albums.filler.storage.FilledStorage
import backend.recon.{Artist, IgnoredReconResult}
import backend.scorer.OptionalModelScore
import genre.{Genre, GenreFinder}
import models.AlbumTitle
import shapeless.syntax.std.tuple.productTupleOps

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.BetterFutureInstances._
import scalaz.ListT
import scalaz.Scalaz.{ToBindOps, ToFunctorOps}

import common.rich.RichEnumeratum.richEnumeratum
import common.rich.RichT.richT

private class NewAlbumsModel @Inject() (
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
  def forArtist(artist: Artist): Future[ArtistAlbums] =
    storage
      .isIgnored(artist)
      .flatMap {
        case IgnoredReconResult.Ignored => Future.successful(NewAlbumsModel.IgnoredArtist)
        case IgnoredReconResult.NotIgnored => albumsForArtist(artist).map(NonIgnoredArtist)
        case IgnoredReconResult.Missing => storage.remove(artist) >| Unreconciled
      }

  def removeArtist(artist: Artist): Future[_] = storage.remove(artist)
  def ignoreArtist(artist: Artist): Future[_] = storage.ignore(artist)
  def unignoreArtist(artist: Artist): Future[Seq[NewAlbum]] =
    storage.unignore(artist) >> albumsForArtist(artist)
  def removeAlbum(artist: Artist, title: AlbumTitle): Future[_] = storage.remove(artist, title)
  def ignoreAlbum(artist: Artist, title: AlbumTitle): Future[_] = storage.ignore(artist, title)
}

private object NewAlbumsModel {
  case class ModelResult(
      artist: Artist,
      artistScore: OptionalModelScore,
      albums: Seq[NewAlbum],
      genre: Option[Genre],
  )
  sealed trait ArtistAlbums
  case class NonIgnoredArtist(albums: Seq[NewAlbum]) extends ArtistAlbums
  case object IgnoredArtist extends ArtistAlbums
  case object Unreconciled extends ArtistAlbums
}
