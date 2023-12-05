package common.io.google

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

import common.io.InternetTalker
import common.io.RichWSResponse._
import play.api.libs.json.JsObject

class GoogleSearch @Inject() private (
    @ApiKey apiKey: String,
    @ApiID apiID: String,
    it: InternetTalker,
) {
  private implicit val iec: ExecutionContext = it
  def apply(
      terms: String,
      resultsPerQuery: Int,
      additionalQueryStringParameters: (String, String)*,
  ): Future[JsObject] = {
    val params = Vector(
      "q" -> terms,
      "cx" -> apiID,
      "key" -> apiKey,
      "num" -> resultsPerQuery.toString,
    ) ++ additionalQueryStringParameters
    it.useWs(
      _.url(GoogleSearch.Url)
        .addQueryStringParameters(params: _*)
        .addHttpHeaders("accept" -> "application/json")
        .get,
    ).map(_.jsonObject)
  }
}

private object GoogleSearch {
  val Url = "https://customsearch.googleapis.com/customsearch/v1"
}
