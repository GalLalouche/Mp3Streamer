package backend.lyrics.retrievers

import backend.configs.Configuration
import common.rich.RichFuture._
import models.Song

private[lyrics] class DefaultArtistInstrumental(implicit c: Configuration) extends DefaultInstrumental {
  private val storage = new InstrumentalArtistStorage()
  override protected def isInstrumental(s: Song) =
    storage.load(s.artistName).map(_.isDefined).get
  override protected val defaultType = "Artist"
}
