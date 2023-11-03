package backend.albums.filler.storage

import backend.albums.filler.{FilterExistingAlbums, NewAlbumRecon}
import backend.albums.ArtistNewAlbums
import backend.module.StandaloneModule
import backend.recon.{Artist, IgnoredReconResult}
import com.google.inject.Guice
import javax.inject.Inject
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector

import scala.concurrent.ExecutionContext

import scalaz.syntax.traverse.ToTraverseOps
import scalaz.Scalaz.{ToApplyOps, ToFunctorOpsUnapply}
import common.rich.func.BetterFutureInstances._
import common.rich.func.MoreTraverseInstances._
import monocle.Monocle.toApplyLensOps

private class CachedNewAlbumStorageImpl @Inject() (
    lastFetchTime: LastFetchTime,
    newAlbumStorage: NewAlbumStorage,
    ec: ExecutionContext,
    filterExistingAlbums: FilterExistingAlbums,
) extends CachedNewAlbumStorage {
  private implicit val iec: ExecutionContext = ec
  override def all = newAlbumStorage.all
    .map { t =>
      t.&|->(ArtistNewAlbums.albums).modify(filterExistingAlbums(t.artist, _))
    }
    .filter(_.albums.nonEmpty)
  override def forArtist(a: Artist) = newAlbumStorage.apply(a).map(filterExistingAlbums(a, _))
  override def freshness(a: Artist) = lastFetchTime.freshness(a)
  override def unremoveAll(a: Artist) = newAlbumStorage.unremoveAll(a)
  override def storeNew(albums: Seq[NewAlbumRecon], artists: Set[Artist]) =
    newAlbumStorage.storeNew(albums).`<*ByName`(artists.traverse(lastFetchTime.update))
  override def newArtist(a: Artist) = reset(a).void
  override def reset(a: Artist) = lastFetchTime.reset(a)
  override def remove(artist: Artist) = newAlbumStorage.remove(artist)
  override def ignore(artist: Artist) = lastFetchTime.ignore(artist)
  override def isIgnored(artist: Artist) = lastFetchTime
    .freshness(artist)
    .run
    .map(
      IgnoredReconResult from _.map(_.localDateTime.isEmpty),
    )
  override def unignore(artist: Artist) = lastFetchTime.unignore(artist).void
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
        .all
        .run
        .get,
    )
  }
}
