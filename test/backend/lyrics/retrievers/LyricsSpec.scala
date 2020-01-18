package backend.lyrics.retrievers

import java.util.regex.Pattern

import backend.external.DocumentSpecs
import backend.lyrics.Instrumental
import backend.lyrics.retrievers.LyricsSpec._
import org.scalatest.{Assertion, Suite}
import org.scalatest.matchers.{BePropertyMatcher, BePropertyMatchResult}

import scala.PartialFunction.cond

import common.rich.primitives.RichString._

trait LyricsSpec extends DocumentSpecs {self: Suite =>
  protected[retrievers] val instrumental: BePropertyMatcher[LyricParseResult] =
    e => BePropertyMatchResult(e == LyricParseResult.Instrumental, "instrumental")
  protected[retrievers] val retrievedInstrumental: BePropertyMatcher[RetrievedLyricsResult] =
    e => BePropertyMatchResult(cond(e) {
      case RetrievedLyricsResult.RetrievedLyrics(Instrumental(_)) => true
    }, "instrumental")
  // TODO match entire lyrics, not just the first lines
  protected[retrievers] def verifyLyrics(
      res: LyricParseResult, firstLine: String, lastLine: String): Assertion = res match {
    case LyricParseResult.Lyrics(l) =>
      val lines = l.split(HtmlNewLine)
      lines.head shouldReturn firstLine
      lines.last shouldReturn lastLine
    case _ => fail(s"Invalid result: <$res>")
  }
  protected[retrievers] def verifyError(result: LyricParseResult): Assertion =
    result shouldBe a[LyricParseResult.Error]
}

private object LyricsSpec {
  private val HtmlNewLine = Pattern.compile("<BR>\r?\n", Pattern.CASE_INSENSITIVE)
}
