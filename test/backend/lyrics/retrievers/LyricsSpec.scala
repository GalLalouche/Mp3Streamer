package backend.lyrics.retrievers

import backend.configs.{Configuration, TestConfiguration}
import backend.external.DocumentSpecs
import org.scalatest.FreeSpec
import org.scalatest.matchers.{BePropertyMatchResult, BePropertyMatcher}

trait LyricsSpec extends FreeSpec with DocumentSpecs {
  protected implicit val config: Configuration = TestConfiguration()
  protected val instrumental: BePropertyMatcher[Option[String]] =
    objectWithProperty => BePropertyMatchResult(objectWithProperty.isEmpty, "instrumental")
  protected def verifyLyrics(html: Option[String], firstLine: String, lastLine: String): Unit = {
    if (html.isEmpty)
      fail("Returned HTML is empty, i.e., is instrumental")
    val lines = html.get.replaceAll("<br>\\s*", "").split("\n").filter(_.nonEmpty).toVector
    lines.head shouldReturn firstLine
    lines.last shouldReturn lastLine
  }
}
