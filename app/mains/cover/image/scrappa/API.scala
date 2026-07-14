package mains.cover.image.scrappa

import com.google.inject.Inject
import mains.cover.image.ImageAPI
import mains.cover.image.scrappa.API.{MinSize, SquareImage}
import play.api.libs.json.JsObject

import scala.concurrent.{ExecutionContext, Future}

import common.io.InternetTalker
import common.io.RichWSResponse._
import common.json.RichJson.ImmutableJsonArray

private class API @Inject() private (
    @ApiKey apiKey: String,
    it: InternetTalker,
    ec: ExecutionContext,
) extends ImageAPI {
  private implicit val iec: ExecutionContext = ec
  // The documentation for scrappa is painfully wrong.
  override def apply(terms: String, pageCount: Int): Future[Seq[JsObject]] =
    it.useWs(
      _.url("https://scrappa.co/api/images")
        .addQueryStringParameters(
          "q" -> terms,
          SquareImage,
          MinSize,
          "page" -> (pageCount + 1).toString,
        )
        .addHttpHeaders("accept" -> "application/json")
        .addHttpHeaders("X-API-KEY" -> apiKey)
        .get(),
    ).map(_.jsonArray.map(_.as[JsObject]))
  override val resultsPerQuery: Int = 10
}

private object API {
  private val SquareImage = "imgar" -> "square"
  private val MinSize = "imgsz" -> "medium"
}
