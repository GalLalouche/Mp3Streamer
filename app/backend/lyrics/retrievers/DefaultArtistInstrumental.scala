package backend.lyrics.retrievers

import backend.configs.Configuration
import backend.lyrics.Instrumental
import common.rich.RichFuture._
import models.Song

import scala.concurrent.Future
import scalaz.std.FutureInstances
import scalaz.syntax.ToBindOps

private[lyrics] class DefaultArtistInstrumental(implicit c: Configuration) extends DefaultInstrumental
    with FutureInstances with ToBindOps {
  private val storage = new InstrumentalArtistStorage()

  override protected def isInstrumental(s: Song) =
    storage.load(s.artistName).map(_.isDefined).get
  override protected val defaultType = "Artist"
  def add(s: Song): Future[Instrumental] = storage.store(s.artistName) >> find(s)
}
