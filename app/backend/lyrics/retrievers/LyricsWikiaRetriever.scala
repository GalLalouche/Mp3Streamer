package backend.lyrics.retrievers

import java.net.URLEncoder
import java.util.NoSuchElementException

import com.google.common.annotations.VisibleForTesting
import javax.inject.Inject
import models.Song
import org.jsoup.nodes.Document

import common.RichJsoup._
import common.rich.RichT._
import common.rich.primitives.RichBoolean._

private class LyricsWikiaRetriever @Inject()(
    singleHostHelper: SingleHostParsingHelper,
) extends HtmlRetriever {
  import LyricsWikiaRetriever._

  override val parse = singleHostHelper(parser)

  private val urlHelper = new SingleHostUrlHelper(url, parse)
  override val get = urlHelper.get
  override val doesUrlMatchHost = or(urlHelper.doesUrlMatchHost, _.address.contains("lyrics.fandom.com/wiki"))
}

private object LyricsWikiaRetriever {
  @VisibleForTesting
  private[retrievers] val url = new SingleHostUrl {
    private def normalize(s: String): String = s.replace(' ', '_').mapTo(URLEncoder.encode(_, "UTF-8"))

    override val hostPrefix: String = "http://lyrics.wikia.com/wiki"
    override def urlFor(s: Song): String = s"$hostPrefix/${normalize(s.artistName)}:${normalize(s.title)}"
  }

  @VisibleForTesting
  private[retrievers] val parser = new SingleHostParser {
    override val source = "LyricsWikia"
    override def apply(d: Document, s: Song): LyricParseResult = {
      val lyrics = d
          .selectSingle(".lyricbox")
          .html
          .split("\n")
          .takeWhile(_.startsWith("<!--").isFalse)
          .filterNot(_.matches("<div class=\"lyricsbreak\"></div>"))
          .mkString("\n")
      if (lyrics.toLowerCase.contains("we are not licensed to display the full lyrics"))
        LyricParseResult.Error(new NoSuchElementException("No actual lyrics (no license)"))
      else if (lyrics contains "TrebleClef") LyricParseResult.Instrumental
      else LyricParseResult.Lyrics(lyrics |> HtmlLyricsUtils.canonize)
    }
  }
}
