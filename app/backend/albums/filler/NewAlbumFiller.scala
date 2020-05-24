package backend.albums.filler

import backend.logging.{FilteringLogger, Logger, LoggingLevel}
import javax.inject.Inject
import net.codingwell.scalaguice.InjectorExtensions._

import scala.concurrent.{ExecutionContext, Future}

import scalaz.syntax.traverse.ToTraverseOps
import common.rich.func.BetterFutureInstances._
import common.rich.func.MoreTraverseInstances._
import common.rich.func.ToMoreFunctorOps

import common.concurrency.DaemonFixedPool
import common.io.JsonableSaver
import common.rich.RichFuture._
import common.rich.RichT._

private class NewAlbumFiller @Inject()(
    cache: ExistingAlbums,
    newAlbumFetcher: NewAlbumFetcher,
    logger: Logger,
    writer: JsonableSaver,
) {
  private implicit val ec: ExecutionContext = DaemonFixedPool(this.simpleName, 10)
  def go(): Future[_] = cache.artists
      .traverse(ToMoreFunctorOps.toProduct(newAlbumFetcher.apply))
      .map {albums =>
        val newAlbums = albums.flatMap(_._2.map(_.newAlbum))
        logger.info(s"Saving new album index with <${newAlbums.size}> new albums")
        writer.save(newAlbums)
      }
}

private object NewAlbumFiller {
  def main(args: Array[String]): Unit = {
    val injector = LocalNewAlbumsModule.overridingStandalone(LocalNewAlbumsModule.default)
    injector.instance[FilteringLogger].setCurrentLevel(LoggingLevel.Verbose)
    implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
    injector.instance[NewAlbumFiller].go().get
    println("Done!")
  }
}
