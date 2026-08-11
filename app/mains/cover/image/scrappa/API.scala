package mains.cover.image.scrappa

import com.google.inject.Inject
import common.io.RichWSResponse._
import common.io.{InternetTalker, PropertiesHelper}
import common.json.RichJson.ImmutableJsonArray
import mains.cover.image.ImageAPI
import mains.cover.image.scrappa.API.{MinSize, SquareImage}
import org.http4s.Status
import play.api.libs.json.JsObject

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

private[image] class API @Inject() private (
    ph: PropertiesHelper,
    it: InternetTalker,
    ec: ExecutionContext,
) extends ImageAPI {
  override val toString = "Scrappa API"
  private implicit val iec: ExecutionContext = ec
  private lazy val apiKey = ph(getClass, "apiKey")
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
        .withRequestTimeout(10.seconds)
        .addHttpHeaders("accept" -> "application/json")
        .addHttpHeaders("X-API-KEY" -> apiKey)
        .get(),
    ).map { r =>
      if (r.status != Status.Ok.code)
        throw new RuntimeException(s"Scrappa API failed: ${r.string}")
      r.jsonArray.map(_.as[JsObject])
    }

  override val resultsPerQuery: Int = 10
}

private object API {
  private val SquareImage = "imgar" -> "square"
  private val MinSize = "imgsz" -> "medium"
}
