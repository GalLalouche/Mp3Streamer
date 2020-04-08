package backend.albums.filler

import java.util.NoSuchElementException

import backend.logging.Logger
import backend.mb.MbAlbumMetadata
import backend.recon.{Album, AlbumReconStorage, Artist, IgnoredReconResult, StoredReconResult}
import com.google.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.scalaFuture.futureInstance
import scalaz.syntax.functor.ToFunctorOps
import scalaz.syntax.traverse.ToTraverseOps
import common.rich.func.MoreSeqInstances._
import common.rich.func.MoreTraverseInstances._
import common.rich.func.ToMoreFunctorOps._
import common.rich.func.ToMoreMonadErrorOps._
import common.rich.func.ToTraverseMonadPlusOps._

import common.concurrency.{DaemonFixedPool, SimpleTypedActor}
import common.rich.primitives.RichBoolean._
import common.rich.RichT._

@Singleton private class AlbumFinisher @Inject()(
    logger: Logger,
    albumReconStorage: AlbumReconStorage,
    cache: ExistingAlbums,
) extends SimpleTypedActor[(Artist, Seq[MbAlbumMetadata]), Seq[NewAlbumRecon]] {
  private implicit val ec: ExecutionContext = DaemonFixedPool(this.simpleName, 10)

  private def store(newAlbumRecon: Seq[NewAlbumRecon]): Future[Unit] = {
    if (newAlbumRecon.isEmpty)
      return Future.successful()
    val artist = newAlbumRecon.head.newAlbum.artist
    logger.verbose(s"Storing albums for <$artist>")
    newAlbumRecon.traverse {newAlbumRecon =>
      val album = newAlbumRecon.newAlbum.toAlbum
      val storeResult = for {
        exists <- albumReconStorage.load(album).map(_.isDefined)
        if exists.isFalse
        _ = logger.verbose(s"Storing <$newAlbumRecon>")
        _ <- albumReconStorage.store(album, StoredReconResult.unignored(newAlbumRecon.reconId))
      } yield ()
      storeResult.handleErrorFlat {
        case _: NoSuchElementException => ()
        case e => logger.error(s"Error while storing <$artist> albums", e)
      }
    }
        .listen(_ => logger.verbose(s"Finished storing <$artist> albums"))
        .void
  }
  private def removeIgnoredAlbums(
      artist: Artist, albums: Seq[MbAlbumMetadata]): Future[Seq[MbAlbumMetadata]] = {
    def toAlbum(album: MbAlbumMetadata) = Album(
      title = album.title, year = album.releaseDate.getYear, artist = artist)
    def isNotIgnored(metadata: MbAlbumMetadata): Future[Boolean] =
      albumReconStorage.isIgnored(toAlbum(metadata))
          .map(_ != IgnoredReconResult.Ignored)
    albums filterTraverse isNotIgnored
  }
  override def !(m: => (Artist, Seq[MbAlbumMetadata])): Future[Seq[NewAlbumRecon]] = {
    val (artist, albums) = m
    if (albums.isEmpty)
      return Future.successful(Nil)
    for {
      removeIgnored <- removeIgnoredAlbums(artist, albums)
      _ = logger.debug(
        s"Finished working on $artist; " +
            s"found ${if (albums.isEmpty) "no" else s"<${albums.size}>"} new albums.")
      removeExisting = cache.removeExistingAlbums(artist, removeIgnored)
      _ <- store(removeExisting)
    } yield removeExisting
  }
}
