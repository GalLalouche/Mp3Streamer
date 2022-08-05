package backend.albums

import backend.albums.filler.storage.FilledStorage
import backend.albums.AlbumsModel.ArtistAlbums
import backend.albums.filler.NewAlbumFiller
import backend.recon.Artist
import backend.scorer.ModelScore
import javax.inject.Inject
import models.{Genre, GenreFinder}
import shapeless.syntax.std.tuple.productTupleOps

import java.time.Duration
import scala.concurrent.{ExecutionContext, Future}

import scalaz.ListT
import scalaz.Scalaz.ToBindOps
import common.rich.func.BetterFutureInstances._

import common.rich.RichT.richT

private class AlbumsModel @Inject()(
    ec: ExecutionContext,
    storage: FilledStorage,
    genreFinder: GenreFinder,
    filler: NewAlbumFiller,
) {
  private implicit val iec: ExecutionContext = ec

  def albums: ListT[Future, ArtistAlbums] =
    storage.all.map(e => e :+ genreFinder.forArtist(e._1) |> Function.tupled(ArtistAlbums.apply))
  def forArtist(artistName: String): Future[Seq[NewAlbum]] = {
    val artist = Artist(artistName).normalized
    filler.update(Duration.ofDays(90), 10)(artist) >> storage.forArtist(artist)
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