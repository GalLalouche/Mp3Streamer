package backend.albums

import backend.albums.NewAlbum.NewAlbumJsonable
import backend.logging.{FilteringLogger, LoggingLevel}
import backend.module.RealModule
import backend.recon.{AlbumReconStorage, StoredReconResult}
import com.google.inject.util.Modules
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.scalaFuture.futureInstance
import common.rich.func.ToMoreFunctorOps._

import common.io.JsonableSaver
import common.rich.RichObservable._

private class NewAlbumFiller @Inject()(
    ec: ExecutionContext,
    retriever: NewAlbumsRetriever,
    albumReconStorage: AlbumReconStorage,
    jsonableSaver: JsonableSaver,
) {
  private implicit val iec: ExecutionContext = ec
  private def store(newAlbumRecon: NewAlbumRecon): Unit = albumReconStorage.store(
    newAlbumRecon.newAlbum.toAlbum, StoredReconResult.unignored(newAlbumRecon.reconId))
  def fetchAndSave: Future[Traversable[NewAlbum]] = retriever.findNewAlbums
      .doOnNext(store)
      .map(_.newAlbum)
      .toFuture[Traversable]
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
