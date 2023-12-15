package backend.lyrics.retrievers

import java.util.regex.Pattern
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

import backend.logging.Logger
import backend.lyrics.{HtmlLyrics, Instrumental, LyricsUrl}
import backend.lyrics.retrievers.SingleHostParsingHelper._
import backend.Url
import common.io.InternetTalker
import common.io.RichWSResponse._
import common.rich.func.BetterFutureInstances._
import common.rich.func.ToMoreMonadErrorOps._
import common.rich.primitives.RichString._
import models.Song
import play.api.http.Status

private class SingleHostParsingHelper @Inject() (it: InternetTalker, logger: Logger) {
  private implicit val iec: ExecutionContext = it

  def apply(p: SingleHostParser)(url: Url, s: Song): Future[RetrievedLyricsResult] = it
    .getAsBrowser(url.toLemonLabs)
    .map(response =>
      if (response.status == Status.NOT_FOUND)
        RetrievedLyricsResult.NoLyrics
      else if (response.status >= 300) {
        logger.warn(s"Got error code <${response.status}> for <$url>")
        RetrievedLyricsResult.NoLyrics
      } else
        p(response.document, s) match {
          case LyricParseResult.Instrumental =>
            RetrievedLyricsResult.RetrievedLyrics(Instrumental(p.source, LyricsUrl.oldUrl(url)))
          case LyricParseResult.Lyrics(l) =>
            RetrievedLyricsResult.RetrievedLyrics(HtmlLyrics(p.source, l, LyricsUrl.oldUrl(url)))
          case LyricParseResult.NoLyrics => RetrievedLyricsResult.NoLyrics
          case LyricParseResult.Error(e) => RetrievedLyricsResult.Error(e)
        },
    )
    .filterWithMessage(
      {
        case RetrievedLyricsResult.RetrievedLyrics(HtmlLyrics(_, h, url)) =>
          h.doesNotMatch(EmptyHtmlRegex)
        case _ => true
      },
      s"Lyrics in $url were empty",
    )
}

private object SingleHostParsingHelper {
  private val EmptyHtmlRegex: Pattern = Pattern.compile("[\\s<br>/]*")
}
