package backend.lyrics.retrievers.genius

import backend.external.DocumentSpecs
import backend.lyrics.{HtmlLyrics, LyricsUrl}
import backend.lyrics.retrievers.RetrievedLyricsResult
import backend.module.StandaloneModule
import com.google.inject.Guice
import io.lemonlabs.uri.Url
import models.SongTagParser
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import org.scalatest.AsyncFreeSpec

import java.io.File

import common.rich.path.RichFile.richFile
import common.test.AsyncAuxSpecs

class IntegrationTest extends AsyncFreeSpec with AsyncAuxSpecs with DocumentSpecs {
  "Unignore to run" ignore {
    val injector = Guice.createInjector(StandaloneModule, GeniusModule)
    val $ = injector.instance[GeniusLyricsRetriever]
    val file = """G:\Media\Music\Rock\Hard-Rock\Guns n' Roses\1987 Appetite for Destruction\06 - Paradise City.mp3"""
    val song = SongTagParser(new File(file))
    $.get(song) shouldEventuallyReturn RetrievedLyricsResult.RetrievedLyrics(HtmlLyrics(
      source = "GeniusLyrics",
      html = getResourceFile("lyrics1.txt").readAll + "\n",
      url = LyricsUrl.Url(Url.parse("https://genius.com/Guns-n-roses-paradise-city-lyrics"))
    ))
  }
}
