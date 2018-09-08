package backend.lyrics.retrievers

import models.Song
import org.jsoup.nodes.Document

private trait SingleHostParser {
  def source: String
  def apply(d: Document, s: Song): LyricParseResult
}
