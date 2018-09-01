package backend.configs

import backend.external.{AlbumExternalStorage, ArtistExternalStorage}
import backend.lyrics.LyricsStorage
import backend.lyrics.retrievers.InstrumentalArtistStorage
import backend.recon.{AlbumReconStorage, ArtistReconStorage}
import com.google.inject.Guice
import common.rich.RichFuture._
import net.codingwell.scalaguice.InjectorExtensions._

import scala.concurrent.ExecutionContext

import scalaz.std.{FutureInstances, ListInstances}
import scalaz.syntax.ToTraverseOps

/** Also creates all needed tables */
object CleanConfiguration extends Configuration
    with FutureInstances with ListInstances with ToTraverseOps {
  override val module = NonPersistentModule
  override val injector = Guice createInjector NonPersistentModule
  // TODO replace with @Inject?
  private def createTables() {
    implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
    List(
      injector.instance[ArtistReconStorage],
      injector.instance[AlbumReconStorage],
      injector.instance[ArtistExternalStorage],
      injector.instance[LyricsStorage],
      injector.instance[InstrumentalArtistStorage],
      injector.instance[AlbumExternalStorage],
    ).traverse(_.utils.createTable()).get
  }
  createTables()
}

