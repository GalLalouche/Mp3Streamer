package backend.albums.filler

import java.util.regex.Pattern

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
    cache: ExistingAlbums,
) {
  private implicit val iec: ExecutionContext = ec
  def findNewAlbums: Observable[Seq[NewAlbumRecon]] = {
    logger.verbose("Getting albums")
    for {
      artist <- Observable from cache.artists
      reconId <- utils getReconId artist
      result <- utils.findNewAlbums(cache, artist, reconId)
    } yield result
  }
}

private object NewAlbumsRetriever {
  def dirToAlbum(dir: DirectoryRef, mf: MusicFinder): Option[Album] = for {
    firstSong <- mf.getSongFilesInDir(dir).headOption
  } yield
    if (dir.name.take(4).forall(_.isDigit)) {
      val split = dir.name.split(" ", 2).ensuring(_.length == 2)
      Album(title = split(1), year = split(0).take(4).toInt, Artist(dir.parent.name))
    } else // Single album artist
      mf.parseSong(firstSong).release
}
