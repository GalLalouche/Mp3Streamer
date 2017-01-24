package mains.albums

import backend.configs.{Configuration, StandaloneConfig}
import backend.mb.MbArtistReconciler
import backend.recon._
import common.Jsonable
import common.RichJson._
import common.ds.RichMap._
import common.io.JsonableSaver
import common.rich.RichFuture._
import common.rich.RichObservable._
import mains.fixer.StringFixer
import models.RealLocations
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.Future
import scalaz.std.FutureInstances
import scalaz.syntax.ToBindOps


object NewAlbums {
  implicit object AlbumJsonable extends Jsonable[Album] {
    override def jsonify(t: Album) =
      Json.obj("artistName" -> t.artist.name, "year" -> t.year, "title" -> t.title)
    override def parse(json: JsObject) =
      Album(json str "title", json int "year", Artist(json str "artistName"))
  }

  def main(args: Array[String]): Unit = {
    implicit val c = StandaloneConfig
    val $ = new NewAlbums()
    $.fetchAndSave.get
  }
}
class NewAlbums(implicit c: Configuration)
    extends ToBindOps with FutureInstances {
  import NewAlbums._
  import c._
  private val logger = c.logger

  private val artistReconStorage = new ArtistReconStorage()
  private val albumReconStorage = new AlbumReconStorage()

  private val jsonableSaver = new JsonableSaver() {
    override protected def jsonFileName[T: Manifest] = {
      s"New${implicitly[Manifest[T]].runtimeClass.getSimpleName.replaceAll("\\$", "")}s.json"
    }
  }

  private def save(m: Map[Artist, Seq[Album]]): Unit = {
    jsonableSaver save m.flatMap(_._2)
  }

  private def ignore[R <: Reconcilable](r: R, reconStorage: ReconStorage[R]): Future[Unit] = {
    logger.debug(s"Ignoring $r")
    reconStorage.load(r).map { existing =>
      assert(existing.isDefined)
      val existingData = existing.get
      assert(existingData._1.isDefined)
      reconStorage.forceStore(r, existingData._1 -> true)
    }
  }

  def removeArtist(a: Artist): Future[Unit] = {
    logger.debug(s"Removing $a")
    load.map(_ - a).map(save)
  }
  def ignoreArtist(a: Artist): Future[Unit] = ignore(a, artistReconStorage) >> removeArtist(a)
  def removeAlbum(a: Album): Future[Unit] = {
    logger.debug(s"Removing $a")
    load.map(_.modified(a.artist, _.filterNot(_ == a))).map(save)
  }
  def ignoreAlbum(a: Album): Future[Unit] = {
    logger.warn(s"Deleting albums ($a) is not supported right now...")
    Future(())
  }

  private val retriever = new NewAlbumsRetriever(
    new ReconcilerCacher(new ArtistReconStorage(), new MbArtistReconciler()), new RealLocations {
      override val subDirs: List[String] = List("Rock", "Metal")
    })
  def fetchAndSave: Future[Traversable[Album]] =
    retriever.findNewAlbums.toFuture[Traversable].consume(jsonableSaver.save(_))
  def load: Future[Map[Artist, Seq[Album]]] = Future(jsonableSaver.loadArray)
      .map(_.map(e => e.copy(artist = Artist(StringFixer(e.artist.name))))
          .groupBy(_.artist)
          .mapValues(_.sortBy(_.year)))
}
