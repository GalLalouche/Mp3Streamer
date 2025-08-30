package backend.search

import com.google.inject.Inject
import models.{AlbumDir, ArtistDir, ModelJsonable, Song}

import scala.collection.mutable

import common.rich.func.MoreTraverseInstances.traversableTraverse
import monocle.Monocle.toApplyLensOps
import monocle.Traversal

import common.TimedLogger
import common.io.{FileRef, JsonableSaver}
import common.json.Jsonable

private class CompositeIndexFactory @Inject() (
    saver: JsonableSaver,
    timedLogger: TimedLogger,
    mj: ModelJsonable,
) {
  import mj._
  def create() = timedLogger("Creating index", scribe.info(_)) {
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
    def loadIndex[T: Jsonable: WeightedIndexable: Manifest: Flyweight] =
      indexBuilder.buildIndexFor {
        val (result, errors) = saver.loadArrayHandleErrors[T]
        errors.foreach(
          scribe.warn("Index parsing error; can be caused by missing/moved files", _),
        )
        result.map(implicitly[Flyweight[T]].replaceWithCached)
      }
    new CompositeIndex(loadIndex[Song], loadIndex[AlbumDir], loadIndex[ArtistDir], mj)
  }
}

private object CompositeIndexFactory {}
