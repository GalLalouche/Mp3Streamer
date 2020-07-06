package backend.albums.filler

import backend.logging.Logger
import backend.mb.MbAlbumMetadata
import backend.recon.{Album, AlbumReconStorage, Artist, IgnoredReconResult, StoredReconResult}
import com.google.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

import scalaz.ListT
import scalaz.syntax.bind.ToBindOps
import scalaz.syntax.functor.ToFunctorOps
import scalaz.syntax.monad.ToMonadOps
import common.rich.func.BetterFutureInstances._
import common.rich.func.MoreSeqInstances._
import common.rich.func.MoreTraverseInstances._
import common.rich.func.ToMoreFunctorOps._
import common.rich.func.ToMoreMonadErrorOps._
import common.rich.func.ToMoreMonadPlusOps._
import common.rich.func.ToMoreMonadTransOps._
import common.rich.func.ToTraverseMonadPlusOps._

import common.concurrency.DaemonFixedPool
import common.rich.RichT._

@Singleton private class AlbumFinisher @Inject()(
    logger: Logger,
    albumReconStorage: AlbumReconStorage,
    cache: EagerExistingAlbums,
) {
  private implicit val ec: ExecutionContext = DaemonFixedPool.single(this.simpleName)

  private def store(newAlbumRecons: Seq[NewAlbumRecon]): Future[Unit] = {
    if (newAlbumRecons.isEmpty)
      return Future.successful()
    val artist = newAlbumRecons.head.newAlbum.artist
    logger.verbose(s"Storing albums for <$artist>")
    (for {
      newAlbumRecon <- newAlbumRecons.toList.hoistId
      album = newAlbumRecon.newAlbum.toAlbum
      _ <- albumReconStorage.exists(album).negated.liftM[ListT].toGuard
      _ = logger.verbose(s"storing <$newAlbumRecon>")
      _ <- albumReconStorage.store(album, StoredReconResult.unignored(newAlbumRecon.reconId)).liftM[ListT]
    } yield ())
        .run
        .listenError(logger.error(s"Error storing albums for <$artist>", _))
        .>|(logger.verbose(s"Finished storing <$artist> albums"))
  }
  private def removeIgnoredAlbums(
      artist: Artist, albums: Seq[MbAlbumMetadata]): Future[Seq[MbAlbumMetadata]] = {
    def toAlbum(album: MbAlbumMetadata) = Album(album.title, album.releaseDate.getYear, artist)
    def isNotIgnored(metadata: MbAlbumMetadata): Future[Boolean] =
      albumReconStorage.isIgnored(toAlbum(metadata))
          .map(_ != IgnoredReconResult.Ignored)
    albums filterM isNotIgnored
  }
  def apply(artist: Artist, albums: Seq[MbAlbumMetadata]): Future[Seq[NewAlbumRecon]] =
    removeIgnoredAlbums(artist, albums)
        .listen(albums => logger.debug(
          s"Finished working on $artist; " +
              s"found ${if (albums.isEmpty) "no" else s"<${albums.size}>"} new albums."
        )).map(cache.removeExistingAlbums(artist, _))
        .>>!(store)
}
