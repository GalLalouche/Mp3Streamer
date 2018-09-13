package backend.albums

import java.util.logging.{Level, Logger => JLogger}

import backend.logging.{FilteringLogger, Logger, LoggingLevel}
import backend.mb.MbArtistReconciler
import backend.module.RealModule
import backend.recon.{Album, AlbumReconStorage, Artist, ArtistReconStorage, Reconcilable, ReconcilerCacher, ReconStorage, StoredReconResult}
import backend.recon.StoredReconResult.{HasReconResult, NoRecon}
import com.google.inject.util.Modules
import common.io.JsonableSaver
import common.rich.RichObservable._
import common.rich.func.ToMoreFunctorOps
import javax.inject.Inject
import mains.fixer.StringFixer
import monocle.function.IndexFunctions
import monocle.syntax.ApplySyntax

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.FutureInstances
import scalaz.syntax.ToBindOps

private class NewAlbums @Inject()(
    ec: ExecutionContext,
    newAlbumsRetrieverFactory: NewAlbumsRetrieverFactory,
    logger: Logger,
    artistReconStorage: ArtistReconStorage,
    albumReconStorage: AlbumReconStorage,
    mbArtistReconciler: MbArtistReconciler,
    jsonableSaver: JsonableSaver,
) extends ToBindOps with ToMoreFunctorOps with FutureInstances
    with ApplySyntax with IndexFunctions {
  import NewAlbum.NewAlbumJsonable

  private implicit val iec: ExecutionContext = ec

  // TODO remove factory
  private val retriever = newAlbumsRetrieverFactory(
    new ReconcilerCacher(artistReconStorage, mbArtistReconciler),
  )

  private def save(m: Map[Artist, Seq[NewAlbum]]): Unit = {
    jsonableSaver save m.flatMap(_._2)
  }

  private def ignore[R <: Reconcilable](r: R, reconStorage: ReconStorage[R]): Future[Unit] = {
    logger.debug(s"Ignoring $r")
    reconStorage.load(r).map {existing =>
      assert(existing.isDefined)
      existing.get match {
        case NoRecon => throw new AssertionError()
        case recon@HasReconResult(_, _) =>
          reconStorage.forceStore(r, recon.ignored)
      }
    }
  }

  def load: Future[Map[Artist, Seq[NewAlbum]]] = Future(jsonableSaver.loadArray)
      .map(_.map(NewAlbum.artist ^|-> Artist.name modify StringFixer.apply)
          .groupBy(_.artist)
          .mapValues(_.sortBy(_.year)))

  def removeArtist(a: Artist): Future[Unit] = {
    logger.debug(s"Removing $a")
    load.map(_ - a).map(save)
  }
  def ignoreArtist(a: Artist): Future[Unit] = ignore(a, artistReconStorage) >> removeArtist(a)
  def removeAlbum(a: Album): Future[Unit] = {
    logger.debug(s"Removing $a")
    load.map(_ &|-? index(a.artist) modify (_.filterNot(_.title == a.title))).map(save)
  }
  def ignoreAlbum(a: Album): Future[Unit] = ignore(a, albumReconStorage) >> removeAlbum(a)

  private def store(newAlbumRecon: NewAlbumRecon): Unit = albumReconStorage.store(
    newAlbumRecon.newAlbum.toAlbum, StoredReconResult.unignored(newAlbumRecon.reconId))
  def fetchAndSave: Future[Traversable[NewAlbum]] =
    retriever.findNewAlbums
        .doOnNext(store)
        .map(_.newAlbum)
        .toFuture[Traversable]
        .listen(jsonableSaver save _)
}

object NewAlbums {
  import com.google.inject.Guice
  import common.rich.RichFuture._
  import net.codingwell.scalaguice.InjectorExtensions._

  def main(args: Array[String]): Unit = {
    JLogger.getLogger("org.jaudiotagger").setLevel(Level.OFF)
    val injector = Guice.createInjector(Modules `override` RealModule `with` LocalNewAlbumsModule)
    val logger = injector.instance[FilteringLogger]
    logger.setCurrentLevel(LoggingLevel.Verbose)
    implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
    injector.instance[NewAlbums].fetchAndSave.get
    println("Done!")
  }
}
