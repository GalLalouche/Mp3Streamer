package backend.albums

import backend.configs.Configuration
import backend.mb.MbArtistReconciler
import backend.recon._
import common.io.FormatSaver
import common.rich.RichFuture._
import common.rich.RichObservable._
import common.rich.func.ToMoreFunctorOps
import mains.fixer.StringFixer
import models.{IOMusicFinder, IOMusicFinderProvider}
import monocle.function.IndexFunctions
import monocle.std.MapOptics
import monocle.syntax.ApplySyntax

import scala.concurrent.Future
import scalaz.std.FutureInstances
import scalaz.syntax.ToBindOps

private class NewAlbums(implicit c: Configuration)
    extends ToBindOps with FutureInstances with MapOptics with ApplySyntax with IndexFunctions
        with ToMoreFunctorOps {
  import NewAlbum.NewAlbumJsonable

  private val logger = c.logger

  private val artistReconStorage = new ArtistReconStorage()
  private val albumReconStorage = new AlbumReconStorage()

  private val jsonableSaver = new FormatSaver()

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
  private val retriever = {
    // SAM doesn't work for zero argument method without parens,
    // and I'll be damned if I'm adding empty parens for this shit
    val mfp = new IOMusicFinderProvider {
      override val mf = new IOMusicFinder {
        override val subDirNames: List[String] = List("Rock", "Metal")
      }
    }
    new NewAlbumsRetriever(new ReconcilerCacher(new ArtistReconStorage(), new MbArtistReconciler()), albumReconStorage)(c, mfp)
  }

  private def store(a: NewAlbum, r: ReconID): Unit = albumReconStorage.store(a.toAlbum, Some(r) -> false)
  def fetchAndSave: Future[Traversable[NewAlbum]] =
    retriever.findNewAlbums
        .doOnEach((store _).tupled)
        .map(_._1)
        .toFuture[Traversable]
        .listen(jsonableSaver save _)
}

object NewAlbums {
  def main(args: Array[String]): Unit = {
    implicit val c: Configuration = NewAlbumsConfig
    val $ = new NewAlbums()
    $.fetchAndSave.get
    println("Done!")
  }
}
