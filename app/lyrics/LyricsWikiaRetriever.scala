package lyrics

import java.io.File
import java.net.URLEncoder
import common.RichFuture._

import common.rich.RichT._
import models.Song
import org.jsoup.nodes.Document

import scala.concurrent.ExecutionContext

private class LyricsWikiaRetriever(implicit ec: ExecutionContext) extends HtmlRetriever {
  override val source = "LyricsWikia"
  override protected def fromHtml(html: Document, s: Song) = html
      .select(".lyricbox")
      .html
      .split("\n")
      .takeWhile(_.startsWith("<!--") == false)
      .mkString("\n")
      .mapTo(Some.apply)
      .filterNot(_ contains "TrebleClef")

  override protected def getUrl(s: Song): String =
    s"http://lyrics.wikia.com/wiki/${normalize(s.artistName)}:${normalize(s.title)}"
  private def normalize(s: String): String = s.replaceAll(" ", "_").mapTo(URLEncoder.encode(_, "UTF-8"))
}

private object LyricsWikiaRetriever extends LyricsWikiaRetriever()(scala.concurrent.ExecutionContext.Implicits.global) {
  import scala.concurrent.ExecutionContext.Implicits.global
  def main(args: Array[String]) {
    val file: File = new File( """D:\Media\Music\Metal\Black Metal\Watain\2010 Lawless Darkness\06 - Lawless Darkness.mp3""")
    println(file.exists())
    println(apply(Song(file)).get)
    println("Done")
  }
}
