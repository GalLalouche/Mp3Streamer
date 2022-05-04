package backend.albums.filler.storage

import backend.albums.filler.{EagerExistingAlbums, NewAlbumRecon}
import backend.module.StandaloneModule
import backend.recon.Artist
import com.google.inject.{Guice, Provider}
import javax.inject.Inject
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector

import scala.concurrent.ExecutionContext

import scalaz.syntax.traverse.ToTraverseOps
import scalaz.Scalaz.ToApplyOps
import common.rich.func.BetterFutureInstances._
import common.rich.func.MoreTraverseInstances._

private class CachedNewAlbumStorageImpl @Inject()(
    lastFetchTime: LastFetchTime,
    newAlbumStorage: NewAlbumStorage,
    ec: ExecutionContext,
    eap: Provider[EagerExistingAlbums],
) extends CachedNewAlbumStorage {
  private lazy val ea = eap.get
  private implicit val iec: ExecutionContext = ec
  override def allRaw = newAlbumStorage.all
  override def allFiltered = newAlbumStorage.all.map {
    case (artist, score, albums) => (artist, score, ea.removeExistingAndUnreleasedAlbums(artist, albums))
  }.filter(_._3.nonEmpty)
  override def apply(a: Artist) = newAlbumStorage.apply(a)
  override def freshness(a: Artist) = lastFetchTime.freshness(a)
  override def unremoveAll(a: Artist) = newAlbumStorage.unremoveAll(a)
  override def storeNew(albums: Seq[NewAlbumRecon], artists: Set[Artist]) =
    newAlbumStorage.storeNew(albums) `<*ByName` artists.traverse(lastFetchTime.update)
  override def reset(a: Artist) = lastFetchTime.reset(a)
  override def remove(artist: Artist) = newAlbumStorage.remove(artist)
  override def ignore(artist: Artist) = lastFetchTime.ignore(artist)
  override def remove(artist: Artist, albumName: String) = newAlbumStorage.remove(artist, albumName)
  override def ignore(artist: Artist, albumName: String) = newAlbumStorage.ignore(artist, albumName)
}

private object CachedNewAlbumStorageImpl {
  def main(args: Array[String]): Unit = {
    import common.rich.RichFuture._
    val injector = Guice.createInjector(StandaloneModule, FillerStorageModule)
    implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
    println(
      injector
          .instance[CachedNewAlbumStorage]
          .allFiltered
          .run
          .get
    )
  }
}
