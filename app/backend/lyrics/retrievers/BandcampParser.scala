package backend.lyrics.retrievers

import java.util.regex.Pattern

import backend.lyrics.retrievers.BandcampParser._
import com.google.common.annotations.VisibleForTesting
import javax.inject.Inject
import models.Song
import org.jsoup.nodes.Document

import common.RichJsoup._
import common.rich.primitives.RichString._

private class BandcampParser @Inject()(helper: SingleHostParsingHelper) extends PassiveParser {
  override val parse = helper(parser)
  override val doesUrlMatchHost = _.address matches UrlPattern
}

private object BandcampParser {
  private val UrlPattern = Pattern compile """.+\.bandcamp.com/track/.*"""
  @VisibleForTesting private[retrievers] val parser = new SingleHostParser {
    override def source = "Bandcamp"
    override def apply(d: Document, s: Song) = LyricParseResult.Lyrics(
      d.selectSingle(".lyricsText").wholeText.simpleReplace("\n", "\n<BR>\n")
    )
  }
}
