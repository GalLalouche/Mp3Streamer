package mains.cover.image

import com.google.inject.Inject
import mains.cover.ImageSource
import mains.cover.image.ImageAPIFetcher.ResultsPerQuery

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.kats.ToMoreMonadErrorOps.toMoreApplicativeErrorOps

import common.io.google.GoogleSearch

private[cover] class ImageAPIFetcher @Inject() (googleSearch: GoogleSearch, ec: ExecutionContext) {
  private implicit val iec: ExecutionContext = ec

  def apply(terms: String, pageCount: Int): Future[Seq[ImageSource]] = googleSearch(
    terms,
    resultsPerQuery = ResultsPerQuery,
    "searchType" -> "image",
    "start" -> (pageCount * ResultsPerQuery + 1).toString,
  ).map(Parser.apply)
    .listenError(e => scribe.error(s"Image API fetch failed for terms: $terms", e))
}

private[cover] object ImageAPIFetcher {
  // Maximum allowed by Google API. I'd have preferred 12, since it's 6 per panel, so one panel +
  // one extra buffer panel, but alas.
  val ResultsPerQuery = 10
}
