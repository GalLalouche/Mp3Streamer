package backend.lyrics

import backend.configs.TestConfiguration
import backend.external.DocumentSpecs
import org.scalatest.FreeSpec
import org.scalatest.matchers.{BePropertyMatchResult, BePropertyMatcher}

trait LyricsSpec extends FreeSpec with DocumentSpecs {
  implicit val c = new TestConfiguration()
  val instrumental = new BePropertyMatcher[Option[String]] {
    override def apply(objectWithProperty: Option[String]) =
      BePropertyMatchResult(objectWithProperty.isEmpty, "instrumental")
  }
  def verifyLyrics(html: Option[String], firstLine: String, lastLine: String) = {
    if (html.isEmpty)
      fail("Returned html is empty, i.e., is instrumental")
    val lines = html.get.replaceAll("<br>\\s*", "").split("\n").filter(_.nonEmpty).toVector
    lines.head shouldReturn firstLine
    lines.last shouldReturn lastLine
  }
}
