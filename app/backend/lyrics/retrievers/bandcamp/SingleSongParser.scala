package backend.lyrics.retrievers.bandcamp

import backend.lyrics.retrievers.{LyricParseResult, SingleHostParser}
import models.Song
import org.jsoup.nodes.Document

import common.RichJsoup._

private object SingleSongParser extends SingleHostParser {
  override def source = Utils.Source
  override def apply(d: Document, s: Song): LyricParseResult =
    Utils.elementToLyrics(d.selectSingle(".lyricstext"))
}
