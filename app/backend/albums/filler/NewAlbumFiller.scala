package backend.albums.filler

import java.util.concurrent.Executors

import backend.logging.{FilteringLogger, LoggingLevel}
import backend.module.StandaloneModule
import com.google.inject.util.Modules
import com.google.inject.Guice
import javax.inject.Inject
import net.codingwell.scalaguice.InjectorExtensions._

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.scalaFuture.futureInstance
import scalaz.syntax.bind._
import scalaz.syntax.traverse.ToTraverseOps
import common.rich.func.MoreTraverseInstances._

import common.rich.RichFuture._
import common.rich.RichT._

private class NewAlbumFiller @Inject()(
    cache: ExistingAlbums,
    newAlbumFetcher: NewAlbumFetcher,
) {
  private implicit val ec: ExecutionContext =
    ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(10,
      (r: Runnable) => new Thread(r, this.simpleName).<|(_.setDaemon(true))))

  def go(): Future[_] = cache.artists.traverse(newAlbumFetcher ! _).void
}

private object NewAlbumFiller {
  def main(args: Array[String]): Unit = {
    val injector = Guice.createInjector(Modules `override` StandaloneModule `with` LocalNewAlbumsModule)
    injector.instance[FilteringLogger].setCurrentLevel(LoggingLevel.Verbose)
    implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
    injector.instance[NewAlbumFiller].go().get
    println("Done!")
  }
}
