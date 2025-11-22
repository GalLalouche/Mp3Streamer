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

import common.concurrency.SimpleTypedActor
import common.io.JsonableSaver
import common.rich.RichT.anyRefT

@Singleton class LastAlbumsState @Inject() (
    lastAlbumProvider: LastAlbumProvider,
    saver: JsonableSaver,
    modelJsonable: ModelJsonable,
    clock: Clock,
    ec: ExecutionContext,
) {
  private implicit val iec: ExecutionContext = ec
  def update(): Future[Seq[AlbumDir]] =
    lastAlbumProvider.last.flatMap(actor ! lastAlbums.enqueue(_)).map(_.albums)
  /** If there's anything to pop, returns the next item and tail. */
  private[last_albums] def dequeue(): FutureOption[(AlbumDir, Seq[AlbumDir])] =
    OptionT(lastAlbums.dequeue.traverse { case (dir, albums) =>
      actor ! albums as (dir, albums.albums)
    })
  private[last_albums] def get: Seq[AlbumDir] = lastAlbums.albums

  import modelJsonable.albumDirJsonifier
  private var lastAlbums = LastAlbums.load(saver, clock)

  private val actor = SimpleTypedActor[LastAlbums, LastAlbums](
    "LastAlbumsState",
    newAlbums => {
      if (lastAlbums.neq(newAlbums)) {
        lastAlbums = newAlbums
        lastAlbums.persist(saver)
      }
      lastAlbums
    },
  )
}
