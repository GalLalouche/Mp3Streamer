package backend.lyrics.retrievers

import com.google.common.annotations.VisibleForTesting
import javax.inject.Inject
import models.Song
import org.jsoup.nodes.Document

import common.rich.RichT._
import common.RichJsoup._

private class ShironetParser @Inject()(helper: SingleHostParsingHelper) extends PassiveParser {
  override def doesUrlMatchHost = _.address contains "shironet.mako.co.il"
  override def parse = helper.apply(ShironetParser.parser)
}

private object ShironetParser {
  @VisibleForTesting private[retrievers] val parser: SingleHostParser = new SingleHostParser {
    override def source = "Shironet"
    override def apply(d: Document, s: Song) =
      d.selectSingle(".artist_lyrics_text").wholeText |>
          HtmlLyricsUtils.addBreakLines |>
          LyricParseResult.Lyrics
  }
}
