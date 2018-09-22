package backend.lyrics.retrievers

import backend.external.DocumentSpecs
import backend.lyrics.Instrumental
import org.scalatest.FreeSpec
import org.scalatest.matchers.{BePropertyMatcher, BePropertyMatchResult}

import scala.PartialFunction.cond

trait LyricsSpec extends FreeSpec with DocumentSpecs {
  protected[retrievers] val instrumental: BePropertyMatcher[LyricParseResult] =
    e => BePropertyMatchResult(e == LyricParseResult.Instrumental, "instrumental")
  protected[retrievers] val retrievedInstrumental: BePropertyMatcher[RetrievedLyricsResult] =
    e => BePropertyMatchResult(cond(e) {
      case RetrievedLyricsResult.RetrievedLyrics(Instrumental(_)) => true
    }, "instrumental")
  protected[retrievers] def verifyLyrics(res: LyricParseResult, firstLine: String, lastLine: String): Unit = res match {
    case LyricParseResult.Lyrics(l) =>
      val lines = l.replaceAll("<br>\\s*", "").split("\n").filter(_.nonEmpty).toVector
      lines.head shouldReturn firstLine
      lines.last shouldReturn lastLine
    case _ => fail(s"Invalid result: <$res>")
  }
  protected[retrievers] def verifyError(result: LyricParseResult): Unit = result shouldBe a[LyricParseResult.Error]
}
