package backend.mb

import common.io.InternetTalker
import common.rich.RichFuture
import common.rich.RichFuture._
import play.api.http.Status
import play.api.libs.json._

import scala.concurrent.Future
import scala.concurrent.duration._

private object JsonHelper {
  def retry[T](f: () => Future[T], times: Int, retryWait: Duration)(implicit it: InternetTalker): Future[T] =
    f().recoverWith {case e =>
      if (times <= 1)
        Future failed new Exception("Failed retry; last failure was: " + e.getMessage)
      else {
        if (!e.isInstanceOf[RichFuture.FilteredException])
          println(e.getMessage)
        Thread sleep retryWait.toMillis
        retry(f, times - 1, retryWait)
      }
    }

  def getJson(method: String, other: (String, String)*)(implicit it: InternetTalker): Future[JsObject] =
    it.useWs(_.url("http://musicbrainz.org/ws/2/" + method)
        .withQueryString(("fmt", "json")).withQueryString(other: _*)
        // see https://musicbrainz.org/doc/XML_Web_Service/Rate_Limiting#How_can_I_be_a_good_citizen_and_be_smart_about_using_the_Web_Service.3FI
        .withHeaders("User-Agent" -> "Mp3Streamer (glpkmtg@gmail.com)").get)
        .filterWithMessage(_.status == Status.OK, "HTTP response wasn't 200: " + _.body)
        .map(_.json.as[JsObject])
}
