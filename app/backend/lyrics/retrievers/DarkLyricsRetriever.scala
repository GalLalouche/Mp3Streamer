package backend.lyrics.retrievers

import java.io.File

import common.io.InternetTalker
import common.rich.RichFuture._
import common.rich.RichT._
import common.rich.primitives.RichBoolean._
import javax.inject.Inject
import models.Song
import org.jsoup.nodes.Document

import scala.concurrent.ExecutionContext

private[lyrics] class DarkLyricsRetriever @Inject()(it: InternetTalker) extends SingleHostHtmlRetriever(it) {
  override protected val source: String = "DarkLyrics"
  private def isInstrumental(html: String) = html.replaceAll("((<br>)|\\n)", "") == "<i>[Instrumental]</i>"
  private def removeWrappingWhiteSpace(s: String) = s.replaceAll("^\\s+", "").replaceAll("\\s$", "")
  private def removeEndingBreaklines(ss: Seq[String]) = ss.reverse.dropWhile(_.matches("<br>")).reverse
  // HTML is structured for shit, so might as well parse it by hand
  override def fromHtml(html: Document, s: Song) = html.toString
      .split("\n").toList
      .dropWhile(_.matches( s""".*a name="${s.track}".*""").isFalse)
      .drop(1)
      .takeWhile(e => e.matches(".*<h3>.*").isFalse && e.matches(".*<div.*").isFalse) // this fucking site...
      .map(removeWrappingWhiteSpace)
      .mapTo(removeEndingBreaklines)
      .mkString("\n")
      .mapTo(Some.apply)
      .filterNot(isInstrumental)

  private def normalize(s: String): String = s.toLowerCase.filter(_.isLetter)
  override protected val hostPrefix: String = "http://www.darklyrics.com/lyrics"
  override def getUrl(s: Song): String =
    s"$hostPrefix/${normalize(s.artistName)}/${normalize(s.albumName)}.html#${s.track}"
}

private[lyrics] object DarkLyricsRetriever {
  // TODO reduce code duplication between all retriever debuggers
  import com.google.inject.Guice
  import backend.module.StandaloneModule
  import net.codingwell.scalaguice.InjectorExtensions._

  def main(args: Array[String]): Unit = {
    val injector = Guice createInjector StandaloneModule
    implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
    val $ = injector.instance[DarkLyricsRetriever]
    val file = """D:\Media\Music\Metal\Progressive Metal\Dream Theater\2003 Train of Thought\05 - Vacant.mp3"""
    println($(Song(new File(file))).get)
  }
}
