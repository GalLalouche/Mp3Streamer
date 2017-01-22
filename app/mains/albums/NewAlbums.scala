package mains.albums

import backend.configs.{Configuration, StandaloneConfig}
import backend.mb.MbArtistReconciler
import backend.recon._
import common.Jsonable
import common.RichJson._
import common.io.JsonableSaver
import common.rich.RichFuture._
import common.rich.RichT._
import mains.fixer.StringFixer
import models.RealLocations
import play.api.libs.json.{JsObject, Json}
import rx.lang.scala.Observable

import scala.concurrent.{Future, Promise}
import scalaz.std.FutureInstances
import scalaz.syntax.ToBindOps


object NewAlbums {
  implicit object AlbumJsonable extends Jsonable[Album] {
    override def jsonify(t: Album) =
      Json.obj("artistName" -> t.artist.name, "year" -> t.year, "title" -> t.title)
    override def parse(json: JsObject) =
      Album(json str "title", json int "year", Artist(json str "artistName"))
  }
  // TODO move to somewhere more general
  private def toFuture[T](o: Observable[T]): Future[Traversable[T]] = {
    val $ = Promise[Traversable[T]]()
    o.toTraversable.subscribe($ success _)
    $.future
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
  private val jsonableSaver = new JsonableSaver() {
    override protected def jsonFileName[T: Manifest] = {
      s"New${implicitly[Manifest[T]].runtimeClass.getSimpleName.replaceAll("\\$", "") }s.json"
    }
  }
  private val retriever = new NewAlbumsRetriever(
    new ReconcilerCacher(new ArtistReconStorage(), new MbArtistReconciler()), new RealLocations {
      override val subDirs: List[String] = List("Rock", "Metal")
    })
  def fetchAndSave: Future[Traversable[Album]] = {
    val $ = retriever.findNewAlbums |> toFuture
    // TODO add consume method to RichFuture
    $.map(e => {jsonableSaver save e; e })
  }
  def load: Future[Map[Artist, Seq[Album]]] = Future(jsonableSaver.loadArray).map { as =>
    as
        .map(e => e.copy(artist = Artist(StringFixer(e.artist.name))))
        .groupBy(_.artist)
        .mapValues(_.sortBy(_.year))
  }
}
