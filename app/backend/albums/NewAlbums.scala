package backend.albums

import backend.albums.NewAlbum.NewAlbumJsonable
import backend.logging.Logger
import backend.recon.{Album, AlbumReconStorage, Artist, ArtistReconStorage, Reconcilable, ReconStorage}
import backend.recon.StoredReconResult.{HasReconResult, NoRecon}
import javax.inject.Inject
import mains.fixer.StringFixer

import scala.concurrent.{ExecutionContext, Future}

import scalaz.syntax.bind.ToBindOps
import scalaz.syntax.functor.ToFunctorOps
import common.rich.func.BetterFutureInstances._
import common.rich.func.RichOptionT._
import monocle.function.Index._
import monocle.syntax.apply.toApplyOptionalOps

import common.io.JsonableSaver

private class NewAlbums @Inject()(
    ec: ExecutionContext,
    logger: Logger,
    artistReconStorage: ArtistReconStorage,
    albumReconStorage: AlbumReconStorage,
    jsonableSaver: JsonableSaver,
) {
  private implicit val iec: ExecutionContext = ec

  private def save(m: Map[Artist, Seq[NewAlbum]]): Unit = jsonableSaver save m.flatMap(_._2)

  private def ignore[R <: Reconcilable](r: R, reconStorage: ReconStorage[R]): Future[Unit] = {
    logger.debug(s"Ignoring $r")
    reconStorage.load(r).get.map {
      case NoRecon => throw new AssertionError()
      case recon@HasReconResult(_, _) =>
        reconStorage.update(r, recon.ignored)
    }.void
  }

  def loadAlbumsByArtist: Future[Map[Artist, Seq[NewAlbum]]] = Future(jsonableSaver.loadArray)
      .map(_.map(NewAlbum.artist ^|-> Artist.name modify StringFixer.apply)
          .groupBy(_.artist)
          .mapValues(_.sortBy(_.year)))

  def removeArtist(a: Artist): Future[Unit] = {
    logger.debug(s"Removing $a")
    loadAlbumsByArtist.map(_.ensuring(_.contains(a)) - a).map(save)
  }
  def ignoreArtist(a: Artist): Future[Unit] = ignore(a, artistReconStorage) >> removeArtist(a)
  def removeAlbum(a: Album): Future[Unit] = {
    logger.debug(s"Removing $a")
    loadAlbumsByArtist.map(_ &|-? index(a.artist) modify (_.filterNot(_.title == a.title))).map(save)
  }
  def ignoreAlbum(a: Album): Future[Unit] = ignore(a, albumReconStorage) >> removeAlbum(a)
}
