package backend.mb

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import common.Jsoner._
import common.RichFuture._
import play.api.http.Status
import play.api.libs.json._
import play.api.libs.ws.ahc.AhcWSClient

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

private object JsonHelper {
  def retry[T](f: () => Future[T], times: Int, retryWait: Duration)(implicit ec: ExecutionContext): Future[T] =
    f().recoverWith { case e =>
      if (times <= 1)
        Future failed new Exception("Failed retry; last failure was: ", e)
      else {
        Thread sleep retryWait.toMillis
        retry(f, times - 1, retryWait)
      }
    }
  
  implicit class RichJson(js: JsValue) {
    def has(str: String): Boolean = {
      val $ = js \ str
      false == $.isInstanceOf[JsUndefined] && $.get != JsNull &&
          ($.get.isInstanceOf[JsString] == false || $.as[String].nonEmpty)
    }
  }
  
  def getJson(method: String, other: (String, String)*)(implicit ec: ExecutionContext): Future[JsValue] = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    val webServiceRequest = AhcWSClient()
        .url("http://musicbrainz.org/ws/2/" + method)
        .withQueryString(("fmt", "json")).withQueryString(other: _*)
        // see https://musicbrainz.org/doc/XML_Web_Service/Rate_Limiting#How_can_I_be_a_good_citizen_and_be_smart_about_using_the_Web_Service.3FI
        .withHeaders(("User-Agent", "Mp3Streamer (glpkmtg@gmail.com)"))
    webServiceRequest.get
        .filterWithMessage(_.status == Status.OK, "HTTP response wasn't 200: " + _.body)
        .map(_.json)
  }
}
