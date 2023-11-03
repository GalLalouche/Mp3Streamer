package backend.lyrics.retrievers

import backend.Url
import com.google.common.annotations.VisibleForTesting
import javax.inject.Inject
import models.Song
import org.jsoup.nodes.Document

import scala.concurrent.Future

import common.RichJsoup._

// Passive since their API costs money to use, which is a pretty dick-move when your lyrics are crowd sourced.
class MusixMatchParser @Inject() (helper: SingleHostParsingHelper) extends PassiveParser {
  override def doesUrlMatchHost: Url => Boolean = _.address.contains("musixmatch.com")
  override def parse: (Url, Song) => Future[RetrievedLyricsResult] =
    helper.apply(MusixMatchParser.parser)
}
object MusixMatchParser {
  @VisibleForTesting
  private[retrievers] val parser: SingleHostParser = new SingleHostParser {
    override def source = "MusixMatch"
    override def apply(d: Document, s: Song) = LyricParseResult.Lyrics(
      HtmlLyricsUtils.addBreakLines(
        d.selectSingle(".mxm-lyrics__content").wholeText(),
      ),
    )
  }
}
