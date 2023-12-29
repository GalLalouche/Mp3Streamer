package backend.lyrics.retrievers.bandcamp

import backend.lyrics.retrievers.{LyricParseResult, SingleHostParser}
import models.Song
import org.jsoup.nodes.Document

import common.rich.func.ToMoreFoldableOps._
import scalaz.std.option.optionInstance

import common.RichJsoup._

private[retrievers] object AlbumParser extends SingleHostParser {
  override def source = Utils.Source
  override def apply(d: Document, s: Song): LyricParseResult =
    d.selectSingleOpt("#lyrics_row_" + s.track)
      .mapHeadOrElse(
        Utils.elementToLyrics,
        LyricParseResult.NoLyrics,
      )
}
