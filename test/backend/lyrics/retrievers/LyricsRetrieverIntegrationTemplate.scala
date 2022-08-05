package backend.lyrics.retrievers

import backend.lyrics.{HtmlLyrics, LyricsUrl}
import backend.module.StandaloneModule
import com.google.inject.Guice
import io.lemonlabs.uri.Url
import models.SongTagParser
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import net.codingwell.scalaguice.ScalaModule
import org.scalatest.{Assertion, AsyncFreeSpec, Suite}

import java.io.File
import scala.concurrent.Future

import common.rich.path.RichFile.richFile
import common.test.AsyncAuxSpecs

trait LyricsRetrieverIntegrationTemplate extends AsyncFreeSpec with AsyncAuxSpecs {self: Suite =>
  def go[R <: HtmlRetriever : Manifest](
      file: String,
      source: String,
      expectedLyricsPath: String,
      expectedUrl: String,
      extraModule: ScalaModule = new ScalaModule {},
  ): Future[Assertion] = {
    val injector = Guice.createInjector(StandaloneModule, extraModule)
    val $ = injector.instance[R]
    val song = SongTagParser(new File(file))
    $.get(song) shouldEventuallyReturn RetrievedLyricsResult.RetrievedLyrics(HtmlLyrics(
      source = source,
      html = getResourceFile(expectedLyricsPath).readAll + "\n",
      url = LyricsUrl.Url(Url.parse(expectedUrl))
    ))
  }
}
