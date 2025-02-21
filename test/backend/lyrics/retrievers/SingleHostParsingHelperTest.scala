package backend.lyrics.retrievers

import java.net.HttpURLConnection

import backend.module.{FakeWSResponse, TestModuleConfiguration}
import io.lemonlabs.uri.Url
import models.{FakeModelFactory, Song}
import net.codingwell.scalaguice.InjectorExtensions._
import org.jsoup.nodes.Document
import org.scalatest.AsyncFreeSpec

import common.test.AsyncAuxSpecs

class SingleHostParsingHelperTest extends AsyncFreeSpec with AsyncAuxSpecs {
  "404 returns NoLyrics" in {
    val injector = TestModuleConfiguration(_urlToResponseMapper = { case _ =>
      FakeWSResponse(status = HttpURLConnection.HTTP_NOT_FOUND)
    }).injector
    val $ = injector.instance[SingleHostParsingHelper]
    val result = $(new SingleHostParser {
      override def source: String = ???
      override def apply(d: Document, s: Song): LyricParseResult = ???
    })(Url("foobar"), new FakeModelFactory().song())
    result shouldEventuallyReturn RetrievedLyricsResult.NoLyrics
  }
}
