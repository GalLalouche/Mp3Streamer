package backend.mb

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scalaz.syntax.monadError.ToMonadErrorOps

import backend.logging.Logger
import backend.mb.JsonDownloader.Input
import common.concurrency.SimpleTypedActor
import common.io.InternetTalker
import common.rich.func.BetterFutureInstances._
import common.rich.func.ToMoreMonadErrorOps._
import common.rich.primitives.RichBoolean._
import play.api.http.Status
import play.api.libs.json._
import play.api.libs.ws.JsonBodyReadables._

@Singleton
private class JsonDownloader @Inject() (it: InternetTalker, logger: Logger) {
  private implicit val ec: ExecutionContext = it

  def apply(method: String, params: (String, String)*): Future[JsObject] =
    actor ! Input(method, params, times = 1)

  private val actor = SimpleTypedActor.asyncRateLimited[Input, JsObject](
    "JsonDownloader",
    { case Input(method, params, times) => aux(method, params, times) },
    1.seconds,
    logger,
  )
  private def aux(method: String, params: Seq[(String, String)], times: Int): Future[JsObject] =
    getJson(method, params).handleError { e =>
      if (times <= 1)
        Future.failed(new Exception(s"Failed retry; last failure was: <${e.getMessage}>"))
      else {
        if (e.isInstanceOf[FilteredException].isFalse)
          println(e.getMessage)
        actor ! Input(method, params, times - 1)
      }
    }

  private def getJson(method: String, params: Seq[(String, String)]): Future[JsObject] =
    it.useWs(
      _.url("http://musicbrainz.org/ws/2/" + method)
        .addQueryStringParameters("fmt" -> "json")
        .addQueryStringParameters(params: _*)
        // see https://musicbrainz.org/doc/XML_Web_Service/Rate_Limiting#How_can_I_be_a_good_citizen_and_be_smart_about_using_the_Web_Service.3FI
        .addHttpHeaders("User-Agent" -> "Mp3Streamer (glpkmtg@gmail.com)")
        .get,
    ).filterWithMessageF(
      _.status == Status.OK,
      m => s"HTTP response wasn't 200, was <${m.status}>: " + m.body,
    ).map(_.body[JsValue].as[JsObject])
}

private object JsonDownloader {
  private case class Input(method: String, params: Seq[(String, String)], times: Int)
}
