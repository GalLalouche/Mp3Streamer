package backend.lyrics.retrievers

import java.net.HttpURLConnection
import java.util.regex.Pattern

import backend.lyrics.{HtmlLyrics, Instrumental, LyricsUrl}
import backend.lyrics.retrievers.SingleHostParsingHelper._
import com.google.inject.Inject
import io.lemonlabs.uri.Url
import models.Song

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.BetterFutureInstances._
import common.rich.func.ToMoreMonadErrorOps._

import common.io.InternetTalker
import common.io.RichWSResponse._
import common.rich.primitives.RichString._

private class SingleHostParsingHelper @Inject() (it: InternetTalker, ec: ExecutionContext) {
  private implicit val iec: ExecutionContext = ec

  def apply(p: SingleHostParser)(url: Url, s: Song): Future[RetrievedLyricsResult] =
    it
      .getAsBrowser(url)
      .map(response =>
        if (response.status == HttpURLConnection.HTTP_NOT_FOUND)
          RetrievedLyricsResult.NoLyrics
        else if (response.status >= 300) {
          scribe.warn(s"Got error code <${response.status}> for <$url>")
          RetrievedLyricsResult.NoLyrics
        } else
          p(response.document, s) match {
            case LyricParseResult.Instrumental =>
              RetrievedLyricsResult.RetrievedLyrics(Instrumental(p.source, LyricsUrl.Url(url)))
            case LyricParseResult.Lyrics(l) =>
              RetrievedLyricsResult.RetrievedLyrics(HtmlLyrics(p.source, l, LyricsUrl.Url(url)))
            case LyricParseResult.NoLyrics => RetrievedLyricsResult.NoLyrics
            case LyricParseResult.Error(e) => RetrievedLyricsResult.Error(e)
          },
      )
      .filterWithMessage(
        {
          case RetrievedLyricsResult.RetrievedLyrics(HtmlLyrics(_, h, _)) =>
            h.doesNotMatch(EmptyHtmlRegex)
          case _ => true
        },
        s"Lyrics in $url were empty",
      )
}

private object SingleHostParsingHelper {
  private val EmptyHtmlRegex: Pattern = Pattern.compile("[\\s<br>/]*")
}
