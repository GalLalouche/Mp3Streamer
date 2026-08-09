package mains.cover.image.serp

import com.google.inject.Inject
import common.io.InternetTalker
import common.io.RichWSResponse._
import common.json.RichJson.DynamicJson
import mains.cover.image.ImageAPI
import mains.cover.image.serp.API.{MinSize, SquareImage}
import play.api.libs.json.JsObject

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

private[image] class API @Inject() private (
    @ApiKey apiKey: String,
    it: InternetTalker,
    ec: ExecutionContext,
) extends ImageAPI {
  override val toString = "Serp API"
  private implicit val iec: ExecutionContext = ec
  override def apply(terms: String, pageCount: Int): Future[Seq[JsObject]] =
    it.useWs(
      _.url("https://serpapi.com/search.json")
        .addQueryStringParameters(
          "engine" -> "google_images",
          "q" -> terms,
          "api_key" -> apiKey,
          SquareImage,
          MinSize,
          // https://serpapi.com/google-images-api#api-parameters-pagination-ijn
          "ijn" -> pageCount.toString,
        )
        .addHttpHeaders("accept" -> "application/json")
        .withRequestTimeout(10.seconds)
        .get(),
    ).map { response =>
      val obj = response.jsonObject
      if (obj.has("error"))
        throw new Exception("API error: " + obj.str("error"))
      obj.objects("images_results")
    }
  override val resultsPerQuery: Int = 100
}
//https://serpapi.com/search.json?engine=google_images&q=flowers&location=Austin,+Texas,+United+States&google_domain=google.com&hl=en&gl=us

private object API {
  // https://serpapi.com/google-images-api#api-parameters-advanced-filters-imgar
  private val SquareImage = "imgar" -> "s"
  // https://serpapi.com/google-images-api#api-parameters-advanced-filters-imgsz 400x300
  private val MinSize = "imgsz" -> "qsvga"
}
