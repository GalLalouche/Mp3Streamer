package backend.albums

import backend.logging.Logger
import backend.recon.{Album, AlbumReconStorage, Artist, ReconcilerCacher}
import backend.recon.Reconcilable.SongExtractor

import common.io.{DirectoryRef, IODirectory}
import common.rich.RichT._
import common.rich.func.{MoreSeqInstances, MoreTraverseInstances, ToMoreFoldableOps, ToMoreFunctorOps, ToMoreMonadErrorOps, ToTraverseMonadPlusOps}
import javax.inject.Inject
import models.{IOMusicFinder, IOSong, MusicFinder}
import rx.lang.scala.Observable

import scala.concurrent.ExecutionContext

import scalaz.std.{FutureInstances, OptionInstances}

private class NewAlbumsRetriever @Inject()(
    albumReconStorage: AlbumReconStorage,
    logger: Logger,
    ec: ExecutionContext,
    mf: MusicFinder,
    reconciler: ReconcilerCacher[Artist],
    utils: NewAlbumsRetrieverUtils,
) extends FutureInstances with MoreTraverseInstances with ToMoreFunctorOps
    with ToTraverseMonadPlusOps with ToMoreMonadErrorOps with MoreSeqInstances
    with ToMoreFoldableOps with OptionInstances {
  private implicit val iec: ExecutionContext = ec
  private val log = logger.verbose _
  private def getExistingAlbums: Seq[Album] = mf.genreDirs
      .flatMap(_.deepDirs)
      .flatMap(NewAlbumsRetriever.dirToAlbum(_, mf))

  def findNewAlbums: Observable[NewAlbumRecon] = {
    log("Creating cache")
    val cache = ArtistLastYearCache from getExistingAlbums
    log("Getting albums")
    for {
      artist <- Observable from cache.artists
      reconId <- utils getReconId artist
      result <- utils.findNewAlbums(cache, artist, reconId)
    } yield result
  }
}

private object NewAlbumsRetriever {
  def dirToAlbum(dir: DirectoryRef, mf: MusicFinder): Option[Album] = dir.files
      .find(_.extension |> mf.extensions)
      .map(mf.parseSong(_).release)
}
