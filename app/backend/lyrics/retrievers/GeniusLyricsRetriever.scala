package backend.lyrics.retrievers

import java.util.regex.Pattern

import com.google.common.annotations.VisibleForTesting
import common.io.InternetTalker
import common.rich.RichT._
import common.rich.collections.RichTraversableOnce._
import javax.inject.Inject
import models.Song
import org.jsoup.nodes.Document

import scala.collection.JavaConverters._

import scalaz.std.ListFunctions

private[lyrics] class GeniusLyricsRetriever @Inject()(
    it: InternetTalker,
    singleHostHelper: SingleHostParsingHelper,
) extends SingleHostHtmlRetriever(it)
    with ListFunctions {
  private def normalize(s: String): String =
    s.filter(e => e.isDigit || e.isLetter || e.isSpaceChar).toLowerCase.replaceAll(" ", "-")
  @VisibleForTesting
  private[retrievers] val parser = new SingleHostUrlHelper {
    private val sectionHeaderRegexp = Pattern.compile("""^\[.*?\]$""")
    override val source = "GeniusLyrics"
    // TODO handle instrumental
    override def apply(d: Document, s: Song): LyricParseResult = LyricParseResult.Lyrics(
      d.select("body .lyrics p:only-child").asScala
          .single
          .children
          .asScala
          .flatMap(e => {
            if (e.outerHtml.toLowerCase == "<br>")
              Some(e.toString)
            else if (e.tag.getName == "a")
              Some(e.html)
            else None
          })
          .filterNot(sectionHeaderRegexp.matcher(_).find())
          .mapIf(_.head == "<br>").to(_.tail)
          .flatMap(e => intersperse(e.split("<br> ").toList, "<br>")) // Some "a"'s can have internal <br>s
          .mkString("\n")
          .replaceAll("(<br>\n?){2,}", "<br>\n<br>\n")
    )
  }
  override val parse = singleHostHelper(parser)
  override protected val hostPrefix: String = "https://genius.com"
  override def getUrl(s: Song) = s"$hostPrefix/${normalize(s"${s.artistName} ${s.title}")}-lyrics"
}
