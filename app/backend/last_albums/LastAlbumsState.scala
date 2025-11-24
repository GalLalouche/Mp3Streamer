package backend.last_albums

import java.time.Clock

import backend.FutureOption
import backend.recent.LastAlbumProvider
import com.google.inject.{Inject, Singleton}
import models.{AlbumDir, ModelJsonable}

import scala.concurrent.{ExecutionContext, Future}

import cats.data.OptionT
import cats.implicits.toTraverseOps
import cats.syntax.functor.toFunctorOps

import common.concurrency.JsonablePersistentValueFactory
import common.rich.RichTime.RichClock

@Singleton class LastAlbumsState @Inject() (
    lastAlbumProvider: LastAlbumProvider,
    modelJsonable: ModelJsonable,
    clock: Clock,
    factory: JsonablePersistentValueFactory,
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

  import LastAlbums.jsonableLastAlbums
  import modelJsonable.albumDirJsonifier

  private val lastAlbums = factory[LastAlbums](new LastAlbums(clock.getLocalDateTime))
}
