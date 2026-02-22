package mains.cover.image

import com.google.inject.Inject
import mains.cover.ImageSource

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.kats.ToMoreMonadErrorOps.toMoreApplicativeErrorOps

private[cover] class ImageAPIFetcher @Inject() (
    apiSearch: SerpAPI,
    ec: ExecutionContext,
) {
  private implicit val iec: ExecutionContext = ec

  def apply(terms: String, pageCount: Int): Future[Seq[ImageSource]] = {
    if (pageCount > 0)
      scribe.trace(s"Fetching page $pageCount for terms: $terms")
    apiSearch(terms, pageCount)
      .map(Parser.apply)
      .listenError(scribe.error(s"Image API fetch failed for terms: $terms", _))
  }
}

private[cover] object ImageAPIFetcher {
  // Maximum allowed by Google API. I'd have preferred 12, since it's 6 per panel, so one panel +
  // one extra buffer panel, but alas.
  val ResultsPerQuery = 100
}
