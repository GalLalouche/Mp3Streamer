package backend.lyrics.retrievers

import backend.Url
import backend.lyrics.{HtmlLyrics, Instrumental, Lyrics}
import common.io.InternetTalker
import common.rich.func.ToMoreFoldableOps
import common.rich.primitives.RichBoolean._
import javax.inject.Inject
import models.Song

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.OptionInstances

// TODO replace with composition
private class SingleHostParsingHelper @Inject()(it: InternetTalker)
    extends ToMoreFoldableOps with OptionInstances {
  private implicit val iec: ExecutionContext = it

  def apply(p: SingleHostUrlHelper)(url: Url, s: Song): Future[Lyrics] = it.downloadDocument(url)
      .map(p(_, s))
      .map {
        case LyricParseResult.Instrumental => Instrumental(p.source)
        case LyricParseResult.Lyrics(l) => HtmlLyrics(p.source, l)
        case LyricParseResult.Error(e) => throw e
      }
      .filter {
        case HtmlLyrics(_, h) => h.matches("[\\s<br>/]*").isFalse
        case _ => true
      }
}
