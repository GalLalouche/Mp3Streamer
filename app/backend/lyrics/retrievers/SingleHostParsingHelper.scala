package backend.lyrics.retrievers

import java.util.regex.Pattern

import common.io.RichWSResponse._
import backend.Url
import backend.logging.Logger
import backend.lyrics.{HtmlLyrics, Instrumental}
import backend.lyrics.retrievers.SingleHostParsingHelper._
import common.io.InternetTalker
import common.rich.primitives.RichString._
import common.rich.func.{ToMoreFoldableOps, ToMoreMonadErrorOps}
import javax.inject.Inject
import models.Song
import play.api.http.Status

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.{FutureInstances, OptionInstances}

private class SingleHostParsingHelper @Inject()(it: InternetTalker, logger: Logger)
    extends ToMoreFoldableOps with OptionInstances
        with ToMoreMonadErrorOps with FutureInstances {
  private implicit val iec: ExecutionContext = it

  def apply(p: SingleHostParser)(url: Url, s: Song): Future[RetrievedLyricsResult] = it.get(url)
      .map(response =>
        if (response.status == Status.NOT_FOUND)
          RetrievedLyricsResult.NoLyrics
        else p(response.document, s) match {
          case LyricParseResult.Instrumental => RetrievedLyricsResult.RetrievedLyrics(Instrumental(p.source))
          case LyricParseResult.Lyrics(l) => RetrievedLyricsResult.RetrievedLyrics(HtmlLyrics(p.source, l))
          case LyricParseResult.NoLyrics => RetrievedLyricsResult.NoLyrics
          case LyricParseResult.Error(e) => RetrievedLyricsResult.Error(e)
        })
      .filterWithMessage({
        case RetrievedLyricsResult.RetrievedLyrics(HtmlLyrics(_, h)) => h doesNotMatch emptyHtmlRegex
        case _ => true
      }, s"Lyrics in $url were empty")
}

private object SingleHostParsingHelper {
  private val emptyHtmlRegex: Pattern = Pattern compile "[\\s<br>/]*"
}
