package mains.cover.image

import com.google.inject.Singleton
import play.api.libs.json.JsObject

import scala.concurrent.{ExecutionContext, Future}

@Singleton private class FallbackImageAPI(main: ImageAPI, fallbackImageAPI: ImageAPI)(implicit
    ec: ExecutionContext,
) extends ImageAPI {
  private var apis: List[ImageAPI] =
    List.fill(3)(Vector(main, fallbackImageAPI)).flatten
  override def apply(terms: String, pageCount: Int): Future[Seq[JsObject]] = apis match {
    case head :: next =>
      head(terms, pageCount).recoverWith { case e =>
        scribe.info(s"Image API <$head> failed, falling back to next API", e)
        apis = next
        this(terms, pageCount)
      }
    case Nil => Future.failed(new RuntimeException("All image APIs failed"))
  }
  override def resultsPerQuery: Int = apis.headOption.map(_.resultsPerQuery).getOrElse(Int.MaxValue)
}
