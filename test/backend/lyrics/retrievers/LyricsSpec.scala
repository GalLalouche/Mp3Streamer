package backend.lyrics.retrievers

import backend.external.DocumentSpecs
import backend.lyrics.Instrumental
import backend.lyrics.retrievers.LyricParseResult.NoLyrics
import backend.lyrics.retrievers.LyricsSpec._
import models.{FakeModelFactory, TrackNumber}
import org.scalatest.{Assertion, Succeeded, Suite}
import org.scalatest.matchers.{BePropertyMatcher, BePropertyMatchResult}
import resource.managed

import scala.PartialFunction.cond
import scala.io.Source

trait LyricsSpec extends DocumentSpecs { self: Suite =>
  private[retrievers] def parser: SingleHostParser
  protected val factory = new FakeModelFactory
  protected[retrievers] def verifyLyrics(htmlFileName: String): Assertion =
    verifyLyrics(htmlFileName = htmlFileName, resultFileName = htmlFileName)
  protected[retrievers] def verifyLyrics(
      htmlFileName: String,
      resultFileName: String,
      trackNumber: TrackNumber = 1,
  ): Assertion = parseDocument(htmlFileName, trackNumber) match {
    case LyricParseResult.Lyrics(l) =>
      // RichFile.readAll doesn't read the final linebreak... Fixing it would probably cause way too many bugs :|
      val contents = managed(Source.fromFile(getResourceFile(resultFileName + ".txt"), "UTF-8"))
        .map(_.mkString)
        .opt
        .get
      l shouldReturn contents
    case res => fail(s"Invalid result: <$res>")
  }
  protected[retrievers] def verifyNoLyrics(
      htmlFileName: String,
      trackNumber: TrackNumber = 1,
  ): Assertion = parseDocument(htmlFileName, trackNumber) match {
    case NoLyrics => Succeeded
    case res => fail(s"Invalid result; expected no lyrics, but got: <$res>")
  }

  protected[retrievers] def verifyInstrumental(
      htmlFileName: String,
      trackNumber: TrackNumber = 1,
  ): Assertion = (parseDocument(htmlFileName, trackNumber) should be).an(instrumental)
  private def parseDocument(htmlFileName: String, trackNumber: TrackNumber = 1): LyricParseResult =
    parser(getDocument(htmlFileName + ".html"), factory.song(trackNumber = trackNumber))
}

private object LyricsSpec {
  protected[retrievers] val instrumental: BePropertyMatcher[LyricParseResult] =
    e => BePropertyMatchResult(e == LyricParseResult.Instrumental, "instrumental")
  protected[retrievers] val retrievedInstrumental: BePropertyMatcher[RetrievedLyricsResult] =
    e =>
      BePropertyMatchResult(
        cond(e) { case RetrievedLyricsResult.RetrievedLyrics(Instrumental(_, _)) =>
          true
        },
        "instrumental",
      )
}
