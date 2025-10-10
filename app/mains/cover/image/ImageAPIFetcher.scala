package mains.cover.image

import com.google.inject.Inject
import mains.cover.ImageSource
import mains.cover.image.ImageAPIFetcher.ResultsPerQuery

import scala.concurrent.{ExecutionContext, Future}

import common.io.google.GoogleSearch

private[cover] class ImageAPIFetcher @Inject() (googleSearch: GoogleSearch, ec: ExecutionContext) {
  private implicit val iec: ExecutionContext = ec

  def apply(terms: String, pageCount: Int): Future[Seq[ImageSource]] = googleSearch(
    terms,
    resultsPerQuery = ResultsPerQuery,
    "searchType" -> "image",
    "start" -> (pageCount * ResultsPerQuery + 1).toString,
  ).map(Parser.apply)
}

private object ImageAPIFetcher {
  private val ResultsPerQuery = 10
}
