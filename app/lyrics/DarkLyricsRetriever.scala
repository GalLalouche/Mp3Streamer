package lyrics

import java.io.File

import models.Song
import common.rich.RichT._
import org.jsoup.nodes.Document

import scala.concurrent.ExecutionContext

import common.RichFuture._

private class DarkLyricsRetriever(implicit ec: ExecutionContext) extends HtmlRetriever {
  override protected val source: String = "DarkLyrics"
  private def removeWrappingWhiteSpace(s: String) = s.replaceAll("^\\s+", "").replaceAll("\\s$", "")
  private def removeEndingBreaklines(ss: Seq[String]) = ss.reverse.dropWhile(_.matches("<br>")).reverse
  // HTML is structured for shit, so might as well parse it by hand
  override protected def fromHtml(html: Document, s: Song) = html.toString
      .split("\n").toList
      .dropWhile(_.matches( s""".*a name="${s.track}".*""") == false)
      .drop(1)
      .takeWhile(e => e.matches(".*<h3>.*") == false && e.matches(".*<div.*") == false) // this fucking site...
      .map(removeWrappingWhiteSpace)
      .mapTo(removeEndingBreaklines)
      .mkString("\n")
      .mapTo(Some.apply)
      .filterNot(_ contains "[Instrumental]")

  private def normalize(s: String): String = s.toLowerCase.filter(_.isLetter)
  override protected def getUrl(s: Song): String =
    s"http://www.darklyrics.com/lyrics/${normalize(s.artistName)}/${normalize(s.albumName)}.html#${s.track}"
}

object DarkLyricsRetriever {
  import scala.concurrent.ExecutionContext.Implicits.global
  def main(args: Array[String]) {
    val $ = new DarkLyricsRetriever()(scala.concurrent.ExecutionContext.Implicits.global)
    println($.apply(Song(new File( """D:\Media\Music\Metal\Progressive Metal\Dream Theater\2003 Train of Thought\05 - Vacant.mp3"""))).get)
  }
}
