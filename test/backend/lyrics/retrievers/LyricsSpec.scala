package backend.lyrics.retrievers

import java.util.regex.Pattern

import backend.external.DocumentSpecs
import backend.lyrics.Instrumental
import backend.lyrics.retrievers.LyricsSpec._
import org.scalatest.{Assertion, Suite}
import org.scalatest.matchers.{BePropertyMatcher, BePropertyMatchResult}

import scala.PartialFunction.cond

import common.rich.RichT._
import common.rich.primitives.RichString._

trait LyricsSpec extends DocumentSpecs {self: Suite =>
  protected[retrievers] val instrumental: BePropertyMatcher[LyricParseResult] =
    e => BePropertyMatchResult(e == LyricParseResult.Instrumental, "instrumental")
  protected[retrievers] val retrievedInstrumental: BePropertyMatcher[RetrievedLyricsResult] =
    e => BePropertyMatchResult(cond(e) {
      case RetrievedLyricsResult.RetrievedLyrics(Instrumental(_)) => true
    }, "instrumental")
  protected[retrievers] def verifyLyrics(
      res: LyricParseResult, firstLine: String, lastLine: String): Assertion = res match {
    case LyricParseResult.Lyrics(l) =>
      // TODO RichString.split
      // TODO Verify the existence of <BR>, otherwise the HTML won't be displayed correctly.
      val lines = l.removeAll(BrPattern).|>(OsAgnosticNewLine.split).filter(_.nonEmpty).toVector
      lines.head shouldReturn firstLine
      lines.last shouldReturn lastLine
    case _ => fail(s"Invalid result: <$res>")
  }
  protected[retrievers] def verifyError(result: LyricParseResult): Assertion =
    result shouldBe a[LyricParseResult.Error]
}

private object LyricsSpec {
  private val BrPattern = Pattern compile "<br>\\s*"
  private val OsAgnosticNewLine = Pattern compile "\r?\n"
}
