package mains.cover.image

import com.google.inject.Inject
import mains.cover.ImageSource

import scala.concurrent.{ExecutionContext, Future}

import common.concurrency.{FutureIterant, Iterant}

private[cover] class ImageAPISearch @Inject() (
    ec: ExecutionContext,
    fetcher: ImageAPIFetcher,
) {
  private implicit val iec: ExecutionContext = ec
  def apply(terms: String, maxCalls: Int): FutureIterant[ImageSource] =
    Iterant
      .from[Future](0)
      .take(maxCalls) // Limit to avoid running out quota
      .flatMap(i => Iterant.from[Future, ImageSource](fetcher(terms, i).map(_.to(LazyList))))
}
