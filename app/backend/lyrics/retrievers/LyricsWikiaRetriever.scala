package backend.lyrics.retrievers

import java.io.File
import java.net.URLEncoder
import java.util.NoSuchElementException

import com.google.common.annotations.VisibleForTesting
import common.io.InternetTalker
import common.rich.RichFuture._
import common.rich.RichT._
import common.rich.primitives.RichBoolean._
import javax.inject.Inject
import models.Song
import org.jsoup.nodes.Document

import scala.concurrent.ExecutionContext

private[lyrics] class LyricsWikiaRetriever @Inject()(
    it: InternetTalker,
    singleHostHelper: SingleHostParsingHelper,
) extends SingleHostHtmlRetriever(it) {
  override protected val hostPrefix: String = "http://lyrics.wikia.com/wiki"
  override def getUrl(s: Song): String =
    s"$hostPrefix/${normalize(s.artistName)}:${normalize(s.title)}"

  private def normalize(s: String): String = s.replaceAll(" ", "_").mapTo(URLEncoder.encode(_, "UTF-8"))

  @VisibleForTesting
  private[retrievers] val parser = new SingleHostUrlHelper {
    override val source = "LyricsWikia"
    override def apply(d: Document, s: Song): LyricParseResult = {
      val lyrics = d
          .select(".lyricbox")
          .html
          .split("\n")
          .takeWhile(_.startsWith("<!--").isFalse)
          .filterNot(_.matches("<div class=\"lyricsbreak\"></div>"))
          .mkString("\n")
      if (lyrics.toLowerCase.contains("we are not licensed to display the full lyrics"))
        LyricParseResult.Error(new NoSuchElementException("No actual lyrics (no license)"))
      else if (lyrics contains "TrebleClef") LyricParseResult.Instrumental
      else LyricParseResult.Lyrics(lyrics)
    }
  }
  override val parse = singleHostHelper(parser)
}

private object LyricsWikiaRetriever {
  import backend.module.StandaloneModule
  import com.google.inject.Guice
  import net.codingwell.scalaguice.InjectorExtensions._

  def main(args: Array[String]): Unit = {
    val injector = Guice createInjector StandaloneModule
    implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
    val $ = injector.instance[LyricsWikiaRetriever]
    val file: File = new File("""D:\Media\Music\Metal\Black Metal\Watain\2010 Lawless Darkness\06 - Lawless Darkness.mp3""")
    println(file.exists())
    println($(Song(file)).get)
    println("Done")
  }
}
