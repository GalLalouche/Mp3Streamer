package mains.cover.image

import com.google.inject.Inject
import mains.cover.image.SerpAPI.{MinSize, SquareImage}
import play.api.libs.json.JsObject

import scala.concurrent.{ExecutionContext, Future}

import common.io.InternetTalker
import common.io.RichWSResponse._

private class SerpAPI @Inject() private (
    @ApiKey apiKey: String,
    it: InternetTalker,
    ec: ExecutionContext,
) {
  private implicit val iec: ExecutionContext = ec
  def apply(terms: String, pageCount: Int): Future[JsObject] =
    it.useWs(
      _.url(SerpAPI.Url)
        .addQueryStringParameters(
          Vector(
            "engine" -> "google_images",
            "q" -> terms,
            "api_key" -> apiKey,
            SquareImage,
            MinSize,
            // https://serpapi.com/google-images-api#api-parameters-pagination-ijn
            "ijn" -> pageCount.toString,
          ): _*,
        )
        .addHttpHeaders("accept" -> "application/json")
        .get(),
    ).map(_.jsonObject)
}
//https://serpapi.com/search.json?engine=google_images&q=flowers&location=Austin,+Texas,+United+States&google_domain=google.com&hl=en&gl=us

private object SerpAPI {
  private val Url = "https://serpapi.com/search.json"
  // https://serpapi.com/google-images-api#api-parameters-advanced-filters-imgar
  private val SquareImage = "imgar" -> "s"
  // https://serpapi.com/google-images-api#api-parameters-advanced-filters-imgsz 400x300
  private val MinSize = "imgsz" -> "qsvga"
}
