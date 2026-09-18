package backend.mb

import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit

import backend.mb.JsonDownloader.{Input, MaxRetries, SleepingUnit}
import com.google.inject.{Inject, Singleton}
import com.google.inject.name.Named
import play.api.libs.json._
import play.api.libs.ws.JsonBodyReadables.readableAsJson

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

import common.rich.func.kats.ToMoreMonadErrorOps.toMoreMonadErrorThrowableOps

import common.concurrency.actor.Actor
import common.io.{InternetTalker, PropertiesHelper}
import common.rich.primitives.RichBoolean.richBoolean

@Singleton // Singleton to ensure rate limiting.
private class JsonDownloader @Inject() (
    // Overriden by tests
    @Named(SleepingUnit) sleepingUnit: TimeUnit,
    ph: PropertiesHelper,
    it: InternetTalker,
    ec: ExecutionContext,
) {
  private implicit val iec: ExecutionContext = ec
  private val userAgent: String = ph.getOrElse(getClass, "userAgent", "no-agent")

  def apply(method: String, params: (String, String)*): Future[JsObject] =
    actor ! Input(method, params)

  private val actor: Actor[Input, JsObject] = Actor
    .exponentialBackoff(
      "JsonDownloader",
      MaxRetries,
      // Allegedly it should be 1 second, but I'm getting too many 503s recently.
      (new FiniteDuration(1, sleepingUnit) * 1.5).asInstanceOf[FiniteDuration],
      e =>
        if (e.isInstanceOf[NoSuchElementException].isFalse)
          e.printStackTrace(),
    )
    .async { case Input(method, params) => getJson(method, params) }

  private def getJson(method: String, params: Seq[(String, String)]): Future[JsObject] =
    it.useWs(
      _.url("http://musicbrainz.org/ws/2/" + method)
        .addQueryStringParameters("fmt" -> "json")
        .addQueryStringParameters(params: _*)
        // see https://musicbrainz.org/doc/XML_Web_Service/Rate_Limiting#How_can_I_be_a_good_citizen_and_be_smart_about_using_the_Web_Service.3FI
        .addHttpHeaders("User-Agent" -> s"Mp3Streamer ($userAgent)")
        .get(),
    ).filterWithMessageF(
      _.status == HttpURLConnection.HTTP_OK,
      m => s"HTTP response for <${m.uri}> wasn't 200, was <${m.status}>: " + m.body,
    ).map(_.body[JsValue].as[JsObject])
}

object JsonDownloader {
  private case class Input(method: String, params: Seq[(String, String)])
  private final val MaxRetries = 3
  final val SleepingUnit = "sleeping_unit"
}
