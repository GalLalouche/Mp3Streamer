package backend.configs

import backend.external.{AlbumExternalStorage, ArtistExternalStorage}
import backend.lyrics.LyricsStorage
import backend.lyrics.retrievers.InstrumentalArtistStorage
import backend.recon.{AlbumReconStorage, ArtistReconStorage}
import backend.storage.DbProvider
import com.google.inject.Guice
import common.rich.RichFuture._
import net.codingwell.scalaguice.InjectorExtensions._

import scala.concurrent.ExecutionContext

import scalaz.std.{FutureInstances, ListInstances}
import scalaz.syntax.ToTraverseOps

/** Also creates all needed tables */
object CleanConfiguration extends RealConfig with NonPersistentConfig
    with FutureInstances with ListInstances with ToTraverseOps {
  override protected val ec: ExecutionContext = ExecutionContext.global
  override val injector = Guice createInjector module
  private val dbP = injector.instance[DbProvider]
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
}

