package backend.lyrics.retrievers.bandcamp

import scalaz.std.option.optionInstance

import backend.lyrics.retrievers.{LyricParseResult, SingleHostParser}
import common.rich.func.ToMoreFoldableOps._
import common.RichJsoup._
import models.Song
import org.jsoup.nodes.Document

private[retrievers] object AlbumParser extends SingleHostParser {
  override def source = Utils.Source
  override def apply(d: Document, s: Song): LyricParseResult =
    d.selectSingleOpt("#lyrics_row_" + s.track)
      .mapHeadOrElse(
        Utils.elementToLyrics,
        LyricParseResult.NoLyrics,
      )
}
