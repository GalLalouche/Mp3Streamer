package backend.lyrics.retrievers

import java.util.regex.Pattern

import common.io.InternetTalker
import common.rich.RichT._
import common.rich.collections.RichTraversableOnce._
import javax.inject.Inject
import models.Song
import org.jsoup.nodes.Document

import scala.collection.JavaConverters._

import scalaz.std.ListFunctions

private[lyrics] class GeniusLyricsRetriever @Inject()(it: InternetTalker) extends SingleHostHtmlRetriever(it)
    with ListFunctions {
  private def normalize(s: String): String =
    s.filter(e => e.isDigit || e.isLetter || e.isSpaceChar).toLowerCase.replaceAll(" ", "-")
  private val sectionHeaderRegexp = Pattern.compile("""^\[.*?\]$""")
  // TODO handle instrumental
  override private[retrievers] def fromHtml(html: Document, s: Song) = LyricParseResult.Lyrics(
    html.select("body .lyrics p:only-child").asScala
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
  override protected val hostPrefix: String = "https://genius.com"
  override def getUrl(s: Song) = s"$hostPrefix/${normalize(s"${s.artistName} ${s.title}")}-lyrics"
  override protected val source = "AZLyrics"
}
