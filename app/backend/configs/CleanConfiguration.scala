package backend.configs

import backend.external.{AlbumExternalStorage, ArtistExternalStorage}
import backend.lyrics.LyricsStorage
import backend.lyrics.retrievers.InstrumentalArtistStorage
import backend.recon.{AlbumReconStorage, ArtistReconStorage}
import com.google.inject.Guice
import common.rich.RichFuture._

import scalaz.std.{FutureInstances, ListInstances}
import scalaz.syntax.ToTraverseOps
import scala.concurrent.ExecutionContext

/** Also creates all needed tables */
object CleanConfiguration extends RealConfig with NonPersistentConfig
    with FutureInstances with ListInstances with ToTraverseOps {
  override protected val ec: ExecutionContext = ExecutionContext.global
  private def createTables() {
    implicit val c: Configuration = this
    List(
      new ArtistReconStorage(),
      new AlbumReconStorage(),
      new ArtistExternalStorage(),
      new LyricsStorage(),
      new InstrumentalArtistStorage(),
      new AlbumExternalStorage(),
    ).traverse(_.utils.createTable()).get
  }
  createTables()

  override val injector = Guice createInjector module
}

