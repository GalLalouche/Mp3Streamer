package backend.lyrics.retrievers.bandcamp

import backend.lyrics.retrievers.{LyricParseResult, SingleHostParser}
import models.Song
import org.jsoup.nodes.Document

import scalaz.std.option.optionInstance
import common.rich.func.ToMoreFoldableOps._

import common.RichJsoup._

private[retrievers] object AlbumParser extends SingleHostParser {
  override def source = Utils.Source
  override def apply(d: Document, s: Song): LyricParseResult = d.selectSingleOpt("#_lyrics_" + s.track)
      .mapHeadOrElse(
        Utils.elementToLyrics,
        LyricParseResult.Error(new NoSuchElementException(s"Could not find song with track <${s.track}>")),
      )
}
