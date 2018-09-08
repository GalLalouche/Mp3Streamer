package backend.lyrics.retrievers

import java.io.File

import com.google.common.annotations.VisibleForTesting
import common.rich.RichT._
import common.rich.primitives.RichBoolean._
import javax.inject.Inject
import models.Song
import org.jsoup.nodes.Document

import scala.concurrent.ExecutionContext

private[lyrics] class DarkLyricsRetriever @Inject()(singleHostHelper: SingleHostParsingHelper)
    extends HtmlRetriever {
  private def isInstrumental(html: String) = html.replaceAll("((<br>)|\\n)", "") == "<i>[Instrumental]</i>"
  private def removeWrappingWhiteSpace(s: String) = s.replaceAll("^\\s+", "").replaceAll("\\s$", "")
  private def removeEndingBreaklines(ss: Seq[String]) = ss.reverse.dropWhile(_.matches("<br>")).reverse

  private def normalize(s: String): String = s.toLowerCase.filter(_.isLetter)

  @VisibleForTesting
  private[retrievers] val parser = new SingleHostParser {
    override val source: String = "DarkLyrics"
    // HTML is structured for shit, so might as well parse it by hand
    override def apply(html: Document, s: Song) = {
      val $ = html.toString
          .split("\n").toList
          .dropWhile(_.matches( s""".*a name="${s.track}".*""").isFalse)
          .drop(1)
          .takeWhile(e => e.matches(".*<h3>.*").isFalse && e.matches(".*<div.*").isFalse) // this fucking site...
          .map(removeWrappingWhiteSpace)
          .mapTo(removeEndingBreaklines)
          .mkString("\n")
      if (isInstrumental($)) LyricParseResult.Instrumental else LyricParseResult.Lyrics($)
    }
  }
  override val parse = singleHostHelper(parser)

  @VisibleForTesting
  private[retrievers] val url = new SingleHostUrl {
    override val hostPrefix: String = "http://www.darklyrics.com/lyrics"
    override def urlFor(s: Song): String =
      s"$hostPrefix/${normalize(s.artistName)}/${normalize(s.albumName)}.html#${s.track}"
  }
  private val urlHelper = new SingleHostUrlHelper(url, parse)
  override val get = urlHelper.get
  override val doesUrlMatchHost = urlHelper.doesUrlMatchHost
}

private[lyrics] object DarkLyricsRetriever {
  // TODO reduce code duplication between all retriever debuggers
  import backend.module.StandaloneModule
  import common.rich.RichFuture._
  import com.google.inject.Guice
  import net.codingwell.scalaguice.InjectorExtensions._

  def main(args: Array[String]): Unit = {
    val injector = Guice createInjector StandaloneModule
    implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
    val $ = injector.instance[DarkLyricsRetriever]
    val file = """D:\Media\Music\Metal\Progressive Metal\Dream Theater\2003 Train of Thought\05 - Vacant.mp3"""
    println($.apply(Song(new File(file))).get)
  }
}
