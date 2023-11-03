package mains.cover.image

import javax.inject.Inject
import mains.cover.image.ImageAPIFetcher.ResultsPerQuery
import play.api.libs.json.JsObject

import scala.concurrent.{ExecutionContext, Future}

import common.io.InternetTalker
import common.io.RichWSResponse._

private[cover] class ImageAPIFetcher @Inject() (
    @ApiKey apiKey: String,
    @ApiID apiID: String,
    it: InternetTalker,
) {
  private implicit val ec: ExecutionContext = it
  def apply(terms: String, pageCount: Int): Future[JsObject] = {
    val url =
      s"https://customsearch.googleapis.com/customsearch/v1"
    it.useWs(
      _.url(url)
        .addQueryStringParameters(
          "q" -> terms,
          "cx" -> apiID,
          "key" -> apiKey,
          "searchType" -> "image",
          "num" -> ResultsPerQuery.toString,
          "start" -> (pageCount * ResultsPerQuery + 1).toString,
        )
        .addHttpHeaders("accept" -> "application/json")
        .get,
    ).map(_.jsonObject)
  }
}

private object ImageAPIFetcher {
  private val ResultsPerQuery = 10
}
