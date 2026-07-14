package mains.cover.image

import play.api.libs.json.JsObject

import scala.concurrent.Future

private trait ImageAPI {
  def apply(terms: String, pageCount: Int): Future[Seq[JsObject]]
  def resultsPerQuery: Int
}
