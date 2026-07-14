package mains.cover.image

import com.google.inject.Inject
import mains.cover.ImageSource

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.kats.ToMoreMonadErrorOps.toMoreApplicativeErrorOps

import common.concurrency.{FutureIterant, Iterant}

private[cover] class ImageSearch @Inject() (
    ec: ExecutionContext,
    imageAPI: ImageAPI,
) {
  private implicit val iec: ExecutionContext = ec
  def apply(terms: String, maxCalls: Int): FutureIterant[ImageSource] =
    Iterant
      .from[Future](0)
      .take(maxCalls) // Limit to avoid running out quota
      .flatMap(i => Iterant.from[Future, ImageSource](imageSources(terms, i).map(_.to(LazyList))))
  def resultsPerQuery: Int = imageAPI.resultsPerQuery
  private def imageSources(terms: String, pageCount: Int): Future[Seq[ImageSource]] =
    imageAPI(terms, pageCount)
      .map(_.map(Parser.apply))
      .listenError(scribe.error(s"Image API fetch failed for terms: $terms", _))
}
