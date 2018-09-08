package backend.lyrics.retrievers

import backend.external.DocumentSpecs
import org.scalatest.FreeSpec
import org.scalatest.matchers.{BePropertyMatcher, BePropertyMatchResult}

trait LyricsSpec extends FreeSpec with DocumentSpecs {
  protected[retrievers] val instrumental: BePropertyMatcher[LyricParseResult] =
    e => BePropertyMatchResult(e == LyricParseResult.Instrumental, "instrumental")
  protected[retrievers] def verifyLyrics(res: LyricParseResult, firstLine: String, lastLine: String): Unit = res match {
    case LyricParseResult.Lyrics(l) =>
      val lines = l.replaceAll("<br>\\s*", "").split("\n").filter(_.nonEmpty).toVector
      lines.head shouldReturn firstLine
      lines.last shouldReturn lastLine
    case _ => fail(s"Invalid result: <$res>")
  }
  protected[retrievers] def verifyError(result: LyricParseResult): Unit = result shouldBe a[LyricParseResult.Error]
}
