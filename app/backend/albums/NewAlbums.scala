package backend.albums

import java.util.logging.{Level, Logger => JLogger}

import backend.configs.RealConfig
import backend.logging.Logger
import backend.mb.MbArtistReconciler
import backend.recon._
import common.io.JsonableSaver
import common.rich.RichFuture._
import common.rich.RichObservable._
import common.rich.func.ToMoreFunctorOps
import mains.fixer.StringFixer
import models.IOMusicFinder
import monocle.function.IndexFunctions
import monocle.syntax.ApplySyntax
import net.codingwell.scalaguice.InjectorExtensions._

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.FutureInstances
import scalaz.syntax.ToBindOps

private class NewAlbums(implicit c: RealConfig)
    extends ToBindOps with ToMoreFunctorOps with FutureInstances
        with ApplySyntax with IndexFunctions {
  import NewAlbum.NewAlbumJsonable

  private val injector = c.injector
  private implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
  private val newAlbumsRetrieverFactory = injector.instance[NewAlbumsRetrieverFactory]
  private val logger = injector.instance[Logger]
  private val artistReconStorage = injector.instance[ArtistReconStorage]
  private val albumReconStorage = injector.instance[AlbumReconStorage]
  private val mbArtistReconciler = injector.instance[MbArtistReconciler]
  private val jsonableSaver = injector.instance[JsonableSaver]
  private val retriever = newAlbumsRetrieverFactory(
    new ReconcilerCacher(artistReconStorage, mbArtistReconciler),
    new IOMusicFinder { // TODO override in mbdule
      override val subDirNames: List[String] = List("Rock", "Metal")
    })

  private def save(m: Map[Artist, Seq[NewAlbum]]): Unit = {
    jsonableSaver save m.flatMap(_._2)
  }

  private def ignore[R <: Reconcilable](r: R, reconStorage: ReconStorage[R]): Future[Unit] = {
    logger.debug(s"Ignoring $r")
    reconStorage.load(r).map {existing =>
      assert(existing.isDefined)
      val existingData = existing.get
      assert(existingData._1.isDefined)
      reconStorage.forceStore(r, existingData._1 -> true)
    }
  }

  def load: Future[Map[Artist, Seq[NewAlbum]]] = Future(jsonableSaver.loadArray)
      .map(_.map(NewAlbum.artist ^|-> Artist.name modify StringFixer.apply)
          .groupBy(_.artist)
          .mapValues(_.sortBy(_.year)))

  def removeArtist(a: Artist): Future[Unit] = {
    logger.debug(s"Removing $a")
    load.map(_ - a).map(save)
  }
  def ignoreArtist(a: Artist): Future[Unit] = ignore(a, artistReconStorage) >> removeArtist(a)
  def removeAlbum(a: Album): Future[Unit] = {
    logger.debug(s"Removing $a")
    load.map(_ &|-? index(a.artist) modify (_.filterNot(_.title == a.title))).map(save)
  }
  def ignoreAlbum(a: Album): Future[Unit] = ignore(a, albumReconStorage) >> removeAlbum(a)

  private def store(a: NewAlbum, r: ReconID): Unit = albumReconStorage.store(a.toAlbum, Some(r) -> false)
  def fetchAndSave: Future[Traversable[NewAlbum]] =
    retriever.findNewAlbums
        .doOnEach((store _).tupled)
        .map(_._1)
        .toFuture[Traversable]
        .listen(jsonableSaver save _)
}

object NewAlbums {
  JLogger.getLogger("org.jaudiotagger").setLevel(Level.OFF)
  def main(args: Array[String]): Unit = {
    implicit val c: RealConfig = NewAlbumsConfig
    implicit val ec: ExecutionContext = c.injector.instance[ExecutionContext]
    new NewAlbums().fetchAndSave.get
    println("Done!")
  }
}
