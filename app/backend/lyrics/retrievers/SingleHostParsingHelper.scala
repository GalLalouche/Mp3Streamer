package backend.lyrics.retrievers

import backend.Url
import backend.logging.Logger
import backend.lyrics.{HtmlLyrics, Instrumental, Lyrics}
import common.io.InternetTalker
import common.rich.func.{ToMoreFoldableOps, ToMoreMonadErrorOps}
import common.rich.primitives.RichBoolean._
import javax.inject.Inject
import models.Song

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.{FutureInstances, OptionInstances}

private class SingleHostParsingHelper @Inject()(it: InternetTalker, logger: Logger)
    extends ToMoreFoldableOps with OptionInstances
        with ToMoreMonadErrorOps with FutureInstances {
  private implicit val iec: ExecutionContext = it

  def apply(p: SingleHostParser)(url: Url, s: Song): Future[Lyrics] = it.downloadDocument(url)
      .map(p(_, s))
      .map {
        case LyricParseResult.Instrumental => Instrumental(p.source)
        case LyricParseResult.Lyrics(l) => HtmlLyrics(p.source, l)
        case LyricParseResult.Error(e) => throw e
      }
      .filterWithMessage({
        case HtmlLyrics(_, h) =>
          h.matches("[\\s<br>/]*").isFalse
        case _ => true
      }, s"Lyrics in $url were empty")
}
