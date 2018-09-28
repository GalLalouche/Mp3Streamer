package backend.lyrics.retrievers

import backend.module.{FakeWSResponse, TestModuleConfiguration}
import backend.Url
import common.rich.RichFuture._
import common.AuxSpecs
import models.{FakeModelFactory, Song}
import net.codingwell.scalaguice.InjectorExtensions._
import org.jsoup.nodes.Document
import org.scalatest.FreeSpec
import play.api.http.Status

import scala.concurrent.ExecutionContext

class SingleHostParsingHelperTest extends FreeSpec with AuxSpecs {
  "404 returns NoLyrics" in {
    val injector = TestModuleConfiguration(_urlToResponseMapper = {
      case _ => FakeWSResponse(status = Status.NOT_FOUND)
    }).injector
    val $ = injector.instance[SingleHostParsingHelper]
    implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
    val result = $(new SingleHostParser {
      override def source: String = ???
      override def apply(d: Document, s: Song): LyricParseResult = ???
    })(Url("foobar"), new FakeModelFactory().song())
    result.get shouldReturn RetrievedLyricsResult.NoLyrics
  }
}
