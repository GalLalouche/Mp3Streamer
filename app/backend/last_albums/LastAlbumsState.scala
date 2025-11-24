package backend.last_albums

import java.time.Clock

import alleycats.Zero
import backend.FutureOption
import backend.recent.LastAlbumProvider
import com.google.inject.{Inject, Singleton}
import models.{AlbumDir, ModelJsonable}

import scala.concurrent.{ExecutionContext, Future}

import cats.data.OptionT
import cats.implicits.{toFunctorOps, toTraverseOps}

import common.json.saver.JsonableCOWFactory
import common.rich.RichTime.RichClock

@Singleton class LastAlbumsState @Inject() (
    lastAlbumProvider: LastAlbumProvider,
    modelJsonable: ModelJsonable,
    clock: Clock,
    factory: JsonableCOWFactory,
    ec: ExecutionContext,
) {
  private implicit val iec: ExecutionContext = ec
  def update(): Future[Seq[AlbumDir]] =
    lastAlbumProvider.last.flatMap(ad => lastAlbums.modify(_.enqueue(ad))).map(_.albums)
  /** If there's anything to pop, returns the next item and tail. */
  private[last_albums] def dequeue(): FutureOption[(AlbumDir, Seq[AlbumDir])] =
    OptionT(lastAlbums.get.dequeue.traverse { case (dir, albums) =>
      lastAlbums set albums as (dir, albums.albums)
    })
  private[last_albums] def get: Seq[AlbumDir] = lastAlbums.get.albums

  private val lastAlbums = {
    import LastAlbums.jsonableLastAlbums
    import modelJsonable.albumDirJsonifier
    implicit val ZeroLastAlbums: Zero[LastAlbums] = new Zero[LastAlbums] {
      def zero: LastAlbums = new LastAlbums(clock.getLocalDateTime)
    }

    factory[LastAlbums]
  }
}
