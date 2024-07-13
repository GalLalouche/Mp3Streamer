package mains.cover.image

import javax.inject.Inject

import mains.cover.image.ImageAPIFetcher.ResultsPerQuery
import play.api.libs.json.JsObject

import scala.concurrent.Future

import common.io.google.GoogleSearch

private[cover] class ImageAPIFetcher @Inject() (googleSearch: GoogleSearch) {
  def apply(terms: String, pageCount: Int): Future[JsObject] = googleSearch(
    terms,
    resultsPerQuery = ResultsPerQuery,
    "searchType" -> "image",
    "start" -> (pageCount * ResultsPerQuery + 1).toString,
  )
}

private object ImageAPIFetcher {
  private val ResultsPerQuery = 10
}
