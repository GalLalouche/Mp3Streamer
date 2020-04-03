package backend.albums.filler

import java.util.NoSuchElementException

import backend.albums.NewAlbum
import backend.albums.NewAlbum.NewAlbumJsonable
import backend.logging.{FilteringLogger, Logger, LoggingLevel}
import backend.module.RealModule
import backend.recon.{AlbumReconStorage, StoredReconResult}
import com.google.inject.util.Modules
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.scalaFuture.futureInstance
import scalaz.syntax.functor.ToFunctorOps
import scalaz.syntax.traverse.ToTraverseOps
import common.rich.func.MoreTraverseInstances._
import common.rich.func.ToMoreFunctorOps._
import common.rich.func.ToMoreMonadErrorOps._

import common.io.JsonableSaver
import common.rich.primitives.RichBoolean._
import common.rich.RichObservable._

private class NewAlbumFiller @Inject()(
    ec: ExecutionContext,
    retriever: NewAlbumsRetriever,
    albumReconStorage: AlbumReconStorage,
    jsonableSaver: JsonableSaver,
    logger: Logger,
) {
  private implicit val iec: ExecutionContext = ec
  private def store(newAlbumRecon: Seq[NewAlbumRecon]): Future[Unit] = {
    val artist = newAlbumRecon.head.newAlbum.artist
    logger.verbose(s"Storing albums for <$artist>")
    newAlbumRecon.traverse {newAlbumRecon =>
      val album = newAlbumRecon.newAlbum.toAlbum
      val storeResult = for {
        exists <- albumReconStorage.load(album).map(_.isDefined)
        if exists.isFalse
        _ = logger.verbose(s"Storing <$newAlbumRecon>")
        _ <- albumReconStorage.store(album, StoredReconResult.unignored(newAlbumRecon.reconId))
      } yield ()
      storeResult.handleErrorFlat {
        case _: NoSuchElementException => ()
        case s => s.printStackTrace()
      }
    }
        .listen(_ => logger.verbose(s"Finished storing <$artist> albums"))
        .void
  }
  def fetchAndSave: Future[Traversable[NewAlbum]] = retriever.findNewAlbums
      .doOnNextAsync(store)
      .flattenElements
      .map(_.newAlbum)
      .toFuture[Stream]
      .listen(jsonableSaver save _)
}

private object NewAlbumFiller {
  import com.google.inject.Guice
  import net.codingwell.scalaguice.InjectorExtensions._

  import common.rich.RichFuture._

  def main(args: Array[String]): Unit = {
    val injector = Guice.createInjector(Modules `override` RealModule `with` LocalNewAlbumsModule)
    injector.instance[FilteringLogger].setCurrentLevel(LoggingLevel.Verbose)
    implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
    injector.instance[NewAlbumFiller].fetchAndSave.get
    println("Done!")
  }
}
