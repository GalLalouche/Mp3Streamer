package backend.lyrics.retrievers

import backend.module.{FakeWSResponse, TestModuleConfiguration}
import backend.Url
import models.{FakeModelFactory, Song}
import net.codingwell.scalaguice.InjectorExtensions._
import org.jsoup.nodes.Document
import org.scalatest.AsyncFreeSpec
import play.api.http.Status

import common.test.AuxSpecs

class SingleHostParsingHelperTest extends AsyncFreeSpec with AuxSpecs {
  "404 returns NoLyrics" in {
    val injector = TestModuleConfiguration(_urlToResponseMapper = {
      case _ => FakeWSResponse(status = Status.NOT_FOUND)
    }).injector
    val $ = injector.instance[SingleHostParsingHelper]
    val result = $(new SingleHostParser {
      override def source: String = ???
      override def apply(d: Document, s: Song): LyricParseResult = ???
    })(Url("foobar"), new FakeModelFactory().song())
    result.map(_ shouldReturn RetrievedLyricsResult.NoLyrics)
  }
}
