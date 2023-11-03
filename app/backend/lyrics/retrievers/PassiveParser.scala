package backend.lyrics.retrievers

import backend.Url
import models.Song

import scala.concurrent.Future

import scalaz.std.option.optionInstance
import common.rich.func.ToMoreFoldableOps._

/** Can parse lyrics but can't actively search for a URL based on song. */
private[lyrics] trait PassiveParser {
  // For point free style
  def doesUrlMatchHost: Url => Boolean
  def parse: (Url, Song) => Future[RetrievedLyricsResult]
}

private object PassiveParser {
  def composite(parsers: PassiveParser*): PassiveParser = new PassiveParser {
    override def doesUrlMatchHost = url => parsers.exists(_.doesUrlMatchHost(url))
    override val parse = (url: Url, s: Song) =>
      parsers
        .find(_.doesUrlMatchHost(url))
        .mapHeadOrElse(
          _.parse(url, s),
          Future.successful(RetrievedLyricsResult.Error.unsupportedHost(url)),
        )
  }
}
