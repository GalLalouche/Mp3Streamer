package backend.new_albums

import java.time.Duration

import backend.mb.AlbumType
import backend.new_albums.NewAlbumsModel.{ArtistAlbums, ModelResult, NonIgnoredArtist, Unreconciled}
import backend.new_albums.filler.NewAlbumFiller
import backend.new_albums.filler.storage.FilledStorage
import backend.recon.{Artist, IgnoredReconResult, ReconID}
import backend.score.OptionalModelScore
import com.google.inject.Inject
import genre.{Genre, GenreFinder}
import shapeless.syntax.std.tuple.productTupleOps

import scala.concurrent.{ExecutionContext, Future}

import cats.implicits.{catsSyntaxFlatMapOps, toFunctorOps}

import common.TempIList.ListT
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
    storage.all.fmap(e =>
      e :+ genreFinder.forArtist(e.artist) |> Function.tupled(ModelResult.apply),
    )
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
        case IgnoredReconResult.Missing => storage.remove(artist) as Unreconciled
      }

  def removeArtist(artist: Artist): Future[_] = storage.remove(artist)
  def ignoreArtist(artist: Artist): Future[_] = storage.ignore(artist)
  def unignoreArtist(artist: Artist): Future[Seq[NewAlbum]] =
    storage.unignore(artist) >> albumsForArtist(artist)
  def removeAlbum(reconID: ReconID): Future[_] = storage.removeAlbum(reconID)
  def ignoreAlbum(reconID: ReconID): Future[_] = storage.ignore(reconID)
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
