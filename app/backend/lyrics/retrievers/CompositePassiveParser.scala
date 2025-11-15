package backend.lyrics.retrievers

import com.google.inject.Inject
import io.lemonlabs.uri.Url
import models.Song

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.kats.ToMoreFoldableOps.toMoreFoldableOps

class CompositePassiveParser @Inject() (parsers: Seq[PassiveParser])(implicit ec: ExecutionContext)
    extends PassiveParser {
  override def doesUrlMatchHost: Url => Boolean = url => parsers.exists(_.doesUrlMatchHost(url))
  override val parse = (url: Url, s: Song) =>
    parsers
      .find(_.doesUrlMatchHost(url))
      .mapHeadOrElse(
        _.parse(url, s),
        Future.successful(RetrievedLyricsResult.Error.unsupportedHost(url)),
      )
}
