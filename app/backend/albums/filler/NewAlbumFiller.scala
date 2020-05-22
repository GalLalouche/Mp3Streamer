package backend.albums.filler

import backend.logging.{FilteringLogger, LoggingLevel}
import javax.inject.Inject
import net.codingwell.scalaguice.InjectorExtensions._

import scala.concurrent.{ExecutionContext, Future}

import scalaz.syntax.functor.ToFunctorOps
import scalaz.syntax.traverse.ToTraverseOps
import common.rich.func.BetterFutureInstances._
import common.rich.func.MoreTraverseInstances._

import common.concurrency.DaemonFixedPool
import common.rich.RichFuture._
import common.rich.RichT._

private class NewAlbumFiller @Inject()(
    cache: ExistingAlbums,
    newAlbumFetcher: NewAlbumFetcher,
) {
  private implicit val ec: ExecutionContext = DaemonFixedPool(this.simpleName, 10)

  def go(): Future[_] = cache.artists.traverse(newAlbumFetcher.apply).void
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
