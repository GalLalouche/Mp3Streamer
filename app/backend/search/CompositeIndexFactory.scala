package backend.search

import com.google.inject.Inject
import models.{AlbumDir, ArtistDir, Song}
import scribe.Level

import scala.collection.mutable

import monocle.Monocle.toApplyLensOps
import monocle.Traversal

import common.TimedLogger
import common.path.ref.FileRef

private class CompositeIndexFactory @Inject() (
    loader: IndexLoader,
    timedLogger: TimedLogger,
) {
  def create(): CompositeIndex = timedLogger("Creating index", Level.Info) {
    val indexBuilder = WeightedIndexBuilder
    val flyweightSongs = mutable.Map[FileRef, Song]()
    trait Flyweight[T] {
      def replaceWithCached(t: T): T
    }
    object Flyweight {
      implicit object SongFlyweight extends Flyweight[Song] {
        override def replaceWithCached(t: Song): Song = flyweightSongs.getOrElseUpdate(t.file, t)
      }
      implicit object AlbumDirFlyweight extends Flyweight[AlbumDir] {
        // Albums aren't cached, since it slows down the index creation too much, and the memory
        // difference isn't that large.
        override def replaceWithCached(t: AlbumDir): AlbumDir = t
          .&|->(AlbumDir.songs)
          .^|->>(Traversal.fromTraverse)
          .modify(SongFlyweight.replaceWithCached)
      }
      implicit object ArtistDirFlyweight extends Flyweight[ArtistDir] {
        override def replaceWithCached(t: ArtistDir): ArtistDir = t
          .&|->(ArtistDir.albums)
          .^|->>(Traversal.fromTraverse)
          .modify(AlbumDirFlyweight.replaceWithCached)
      }
    }
    def flyweight[A: WeightedIndexable: Flyweight](xs: Seq[A]) =
      indexBuilder.buildIndexFor(xs.map(implicitly[Flyweight[A]].replaceWithCached))
    new CompositeIndex(
      flyweight(loader.loadSongs),
      flyweight(loader.loadAlbums),
      flyweight(loader.loadArtists),
    )
  }
}
