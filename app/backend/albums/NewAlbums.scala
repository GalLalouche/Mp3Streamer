package backend.albums

import backend.configs.{Configuration, StandaloneConfig}
import backend.mb.MbArtistReconciler
import backend.recon._
import common.ds.RichMap._
import common.io.JsonableSaver
import common.rich.RichFuture._
import common.rich.RichObservable._
import mains.fixer.StringFixer
import models.IOMusicFinder

import scala.collection.JavaConversions._
import scala.concurrent.Future
import scalaz.std.FutureInstances
import scalaz.syntax.ToBindOps

class NewAlbums(implicit c: Configuration)
    extends ToBindOps with FutureInstances {
  import NewAlbum.NewAlbumJsonable
  private val logger = c.logger
  import c._

  private val artistReconStorage = new ArtistReconStorage()
  private val albumReconStorage = new AlbumReconStorage()

  private val jsonableSaver = new JsonableSaver()

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
      //TODO lenses
      .map(_.map(e => e.copy(artist = Artist(StringFixer(e.artist.name))))
      .groupBy(_.artist)
      .mapValues(_.sortBy(_.year)))

  def removeArtist(a: Artist): Future[Unit] = {
    logger.debug(s"Removing $a")
    load.map(_ - a).map(save)
  }
  def ignoreArtist(a: Artist): Future[Unit] = ignore(a, artistReconStorage) >> removeArtist(a)
  def removeAlbum(a: Album): Future[Unit] = {
    logger.debug(s"Removing $a")
    load.map(_.modified(a.artist, _.filterNot(_.title == a.title))).map(save)
  }
  def ignoreAlbum(a: Album): Future[Unit] = ignore(a, albumReconStorage) >> removeAlbum(a)
  implicit val locations = new IOMusicFinder {
    override val subDirNames: List[String] = List("Rock", "Metal")
  }

  private val retriever =
    new NewAlbumsRetriever(new ReconcilerCacher(new ArtistReconStorage(), new MbArtistReconciler()), albumReconStorage)(
      c, locations)

  private def store(a: NewAlbum, r: ReconID): Unit = albumReconStorage.store(a.toAlbum, Some(r) -> false)
  def fetchAndSave: Future[Traversable[NewAlbum]] =
    retriever.findNewAlbums
        .doOnEach((store _).tupled)
        .map(_._1)
        .toFuture[Traversable]
        .consume(jsonableSaver save _)
}

object NewAlbums {
  def main(args: Array[String]): Unit = {
    implicit val c = StandaloneConfig
    val $ = new NewAlbums()
    $.fetchAndSave.get
    println("Done!")
    Thread.getAllStackTraces.keySet.filterNot(_.isDaemon).foreach(println)
  }
}
