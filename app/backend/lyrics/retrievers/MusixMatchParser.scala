package backend.lyrics.retrievers

import javax.inject.Inject
import scala.concurrent.Future

import com.google.common.annotations.VisibleForTesting
import common.RichJsoup._
import io.lemonlabs.uri.Url
import models.Song
import org.jsoup.nodes.Document

// Passive since their API costs money to use, which is a pretty dick-move when your lyrics are crowd sourced.
class MusixMatchParser @Inject() (helper: SingleHostParsingHelper) extends PassiveParser {
  override def doesUrlMatchHost: Url => Boolean = _.toStringPunycode.contains("musixmatch.com")
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
