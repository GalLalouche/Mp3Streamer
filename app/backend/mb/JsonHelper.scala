package backend.mb

import common.io.InternetTalker
import common.rich.func.ToMoreMonadErrorOps
import common.rich.primitives.RichBoolean._
import play.api.http.Status
import play.api.libs.json._
import play.api.libs.ws.JsonBodyReadables._

import scala.concurrent.Future
import scala.concurrent.duration._
import scalaz.std.FutureInstances

private object JsonHelper extends ToMoreMonadErrorOps with FutureInstances {
  def retry[T](f: () => Future[T], times: Int, retryWait: Duration)(implicit it: InternetTalker): Future[T] =
    f().recoverWith {case e =>
      if (times <= 1)
        Future failed new Exception("Failed retry; last failure was: " + e.getMessage)
      else {
        if (e.isInstanceOf[FilteredException].isFalse)
          println(e.getMessage)
        Thread sleep retryWait.toMillis
        retry(f, times - 1, retryWait)
      }
    }

  def getJson(method: String, other: (String, String)*)(implicit it: InternetTalker): Future[JsObject] =
    it.useWs(_.url("http://musicbrainz.org/ws/2/" + method)
        .addQueryStringParameters(("fmt", "json")).addQueryStringParameters(other: _*)
        // see https://musicbrainz.org/doc/XML_Web_Service/Rate_Limiting#How_can_I_be_a_good_citizen_and_be_smart_about_using_the_Web_Service.3FI
        .addHttpHeaders("User-Agent" -> "Mp3Streamer (glpkmtg@gmail.com)").get)
        .filterWithMessageF(_.status == Status.OK, "HTTP response wasn't 200: " + _.body)
        .map(_.body[JsValue].as[JsObject])
}
