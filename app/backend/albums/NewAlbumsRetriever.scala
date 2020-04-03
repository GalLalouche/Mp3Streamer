package backend.albums

import backend.logging.Logger
import backend.recon.{Album, AlbumReconStorage, Artist, ReconcilerCacher}
import backend.recon.Reconcilable.SongExtractor
import javax.inject.Inject
import models.MusicFinder
import rx.lang.scala.Observable

import scala.concurrent.ExecutionContext

import common.io.DirectoryRef

private class NewAlbumsRetriever @Inject()(
    albumReconStorage: AlbumReconStorage,
    logger: Logger,
    ec: ExecutionContext,
    mf: MusicFinder,
    reconciler: ReconcilerCacher[Artist],
    utils: NewAlbumsRetrieverUtils,
    cache: ExistingAlbumsCache,
) {
  private implicit val iec: ExecutionContext = ec
  def findNewAlbums: Observable[NewAlbumRecon] = {
    logger.verbose("Getting albums")
    for {
      artist <- Observable from cache.artists
      reconId <- utils getReconId artist
      result <- utils.findNewAlbums(cache, artist, reconId)
    } yield result
  }
}

private object NewAlbumsRetriever {
  def dirToAlbum(dir: DirectoryRef, mf: MusicFinder): Option[Album] =
    mf.getSongsInDir(dir).headOption.map(_.release)
}
