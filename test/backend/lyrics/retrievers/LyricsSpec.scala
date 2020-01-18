package backend.lyrics.retrievers

import java.util.regex.Pattern

import backend.external.DocumentSpecs
import backend.lyrics.Instrumental
import org.scalatest.{Assertion, Suite}
import org.scalatest.matchers.{BePropertyMatcher, BePropertyMatchResult}
import resource.managed

import scala.PartialFunction.cond
import scala.io.Source

trait LyricsSpec extends DocumentSpecs {self: Suite =>
  protected[retrievers] val instrumental: BePropertyMatcher[LyricParseResult] =
    e => BePropertyMatchResult(e == LyricParseResult.Instrumental, "instrumental")
  protected[retrievers] val retrievedInstrumental: BePropertyMatcher[RetrievedLyricsResult] =
    e => BePropertyMatchResult(cond(e) {
      case RetrievedLyricsResult.RetrievedLyrics(Instrumental(_)) => true
    }, "instrumental")
  protected[retrievers] def verifyLyrics(res: LyricParseResult, resultPath: String): Assertion = res match {
    case LyricParseResult.Lyrics(l) =>
      // RichFile.readAll doesn't read the final linebreak... Fixing it would probably cause way too many bugs :|
      val contents = managed(Source.fromFile(getResourceFile(resultPath), "UTF-8")).map(_.mkString).opt.get
      l shouldReturn contents
    case _ => fail(s"Invalid result: <$res>")
  }
  protected[retrievers] def verifyError(result: LyricParseResult): Assertion =
    result shouldBe a[LyricParseResult.Error]
}

private object LyricsSpec {
  private val HtmlNewLine = Pattern.compile("<BR>\r?\n", Pattern.CASE_INSENSITIVE)
}
