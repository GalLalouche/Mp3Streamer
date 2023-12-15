package backend.lyrics.retrievers

import org.scalatest.AsyncFreeSpec

import backend.module.{FakeWSResponse, TestModuleConfiguration}
import common.test.AsyncAuxSpecs
import io.lemonlabs.uri.Url
import models.{FakeModelFactory, Song}
import net.codingwell.scalaguice.InjectorExtensions._
import org.jsoup.nodes.Document
import play.api.http.Status

class SingleHostParsingHelperTest extends AsyncFreeSpec with AsyncAuxSpecs {
  "404 returns NoLyrics" in {
    val injector = TestModuleConfiguration(_urlToResponseMapper = { case _ =>
      FakeWSResponse(status = Status.NOT_FOUND)
    }).injector
    val $ = injector.instance[SingleHostParsingHelper]
    val result = $(new SingleHostParser {
      override def source: String = ???
      override def apply(d: Document, s: Song): LyricParseResult = ???
    })(Url("foobar"), new FakeModelFactory().song())
    result shouldEventuallyReturn RetrievedLyricsResult.NoLyrics
  }
}
