package mains.cover.image

import com.google.inject.Singleton
import play.api.libs.json.JsObject

import java.util.concurrent.atomic.AtomicBoolean
import scala.concurrent.{ExecutionContext, Future}

@Singleton private class FallbackImageAPI(main: ImageAPI, fallbackImageAPI: ImageAPI)(implicit
    ec: ExecutionContext,
) extends ImageAPI {
  private val mainHasFailed = new AtomicBoolean(false)
  override def apply(terms: String, pageCount: Int): Future[Seq[JsObject]] =
    if (mainHasFailed.get())
      fallbackImageAPI(terms, pageCount)
    else
      main(terms, pageCount).recoverWith { case e =>
        mainHasFailed.set(true)
        scribe.info(s"Main image API <$main> failed, falling back to <$fallbackImageAPI>", e)
        fallbackImageAPI(terms, pageCount)
      }
  override def resultsPerQuery: Int =
    Math.min(main.resultsPerQuery, fallbackImageAPI.resultsPerQuery)
}
