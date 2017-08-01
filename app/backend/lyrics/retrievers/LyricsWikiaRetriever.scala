package backend.lyrics.retrievers

import java.io.File
import java.net.URLEncoder

import backend.configs.StandaloneConfig
import common.io.InternetTalker
import common.rich.RichFuture._
import common.rich.RichT._
import models.Song
import org.jsoup.nodes.Document

import scala.concurrent.ExecutionContext

private[lyrics] class LyricsWikiaRetriever(implicit ec: ExecutionContext, it: InternetTalker) extends SingleHostHtmlRetriever {
  override val source = "LyricsWikia"
  override def fromHtml(html: Document, s: Song) = html
      .select(".lyricbox")
      .html
      .split("\n")
      .takeWhile(_.startsWith("<!--") == false)
      .filterNot(_.matches("<div class=\"lyricsbreak\"></div>"))
      .mkString("\n")
      .mapTo(Some.apply)
      .filterNot(_ contains "TrebleClef")
  override protected val hostPrefix: String = "http://lyrics.wikia.com/wiki"
  override def getUrl(s: Song): String =
    s"$hostPrefix/${normalize(s.artistName) }:${normalize(s.title) }"

  private def normalize(s: String): String = s.replaceAll(" ", "_").mapTo(URLEncoder.encode(_, "UTF-8"))
}

private object LyricsWikiaRetriever {
  def main(args: Array[String]) {
    implicit val c = StandaloneConfig
    val $ = new LyricsWikiaRetriever()
    val file: File = new File("""D:\Media\Music\Metal\Black Metal\Watain\2010 Lawless Darkness\06 - Lawless Darkness.mp3""")
    println(file.exists())
    println($(Song(file)).get)
    println("Done")
  }
}
