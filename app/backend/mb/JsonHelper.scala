package backend.mb

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import common.rich.RichFuture._
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

  private implicit lazy val system = ActorSystem("JsonHelper")
  def getJson(method: String, other: (String, String)*)(implicit ec: ExecutionContext): Future[JsValue] = {
    val c = AhcWSClient()(ActorMaterializer())
    c.url("http://musicbrainz.org/ws/2/" + method)
        .withQueryString(("fmt", "json")).withQueryString(other: _*)
        // see https://musicbrainz.org/doc/XML_Web_Service/Rate_Limiting#How_can_I_be_a_good_citizen_and_be_smart_about_using_the_Web_Service.3FI
        .withHeaders("User-Agent" -> "Mp3Streamer (glpkmtg@gmail.com)").get
        .map(e => {
          c.close()
          e
        })
        .filterWithMessage(_.status == Status.OK, "HTTP response wasn't 200: " + _.body)
        .map(_.json)
  }
}
