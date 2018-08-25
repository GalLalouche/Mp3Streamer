package backend.lyrics.retrievers

import backend.configs.Configuration
import backend.lyrics.Instrumental
import common.rich.RichFuture._
import models.Song
import net.codingwell.scalaguice.InjectorExtensions._

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.FutureInstances
import scalaz.syntax.ToBindOps

private[lyrics] class InstrumentalArtist(implicit c: Configuration) extends DefaultInstrumental
    with FutureInstances with ToBindOps {
  private implicit val ec: ExecutionContext = c.injector.instance[ExecutionContext]
  private val storage = c.injector.instance[InstrumentalArtistStorage]

  override protected def isInstrumental(s: Song) =
    storage.load(s.artistName).map(_.isDefined).get
  override protected val defaultType = "Artist"
  def add(s: Song): Future[Instrumental] = storage.store(s.artistName) >> apply(s)
}
