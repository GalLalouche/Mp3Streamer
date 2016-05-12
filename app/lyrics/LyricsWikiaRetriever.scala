package lyrics

import java.io.File
import java.net.URLEncoder

import common.RichFuture._
import common.rich.RichT._
import models.Song
import org.jsoup.Jsoup

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class LyricsWikiaRetriever(implicit ec: ExecutionContext) extends (Song => Future[Lyrics]) {
  private def parse(html: String): Lyrics =
    Jsoup.parse(html)
        .select(".lyricbox")
        .html
        .split("\n")
        .takeWhile(_.startsWith("<!--") == false)
        .mkString("\n")
        .mapTo(new Lyrics(_))
  private def normalize(s: String): String = s.replaceAll(" ", "_").mapTo(URLEncoder.encode(_, "UTF-8"))
  override def apply(s: Song): Future[Lyrics] = {
    val path = s"${normalize(s.artistName)}:${normalize(s.title)}"
    Future.apply(scala.io.Source.fromURL("http://lyrics.wikia.com/wiki/" + path.log(), "UTF-8"))
        .map(_.mkString)
        .filterWithMessage(_.matches("\\s*") == false, e => "Was empty")
        .map(parse)
        .filterWithMessage(_.html.matches("\\s*") == false, e => "html was invalid")
  }
}

object LyricsWikiaRetriever extends LyricsWikiaRetriever() {
  def main(args: Array[String]) {
    println(apply(Song(new File( """D:\Media\Music\Rock\Neo-Prog\The Flower Kings\1997 Stardust We are\05 - Poor Mr. Rain's Ordinary Guitar.mp3"""))).get)
    println("Done")
  }
}
