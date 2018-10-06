package controllers

import akka.stream.scaladsl.Source
import akka.util.ByteString
import javax.inject.Inject
import play.api.http.{HttpEntity, Writeable}
import play.api.libs.iteratee.streams.IterateeStreams
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.JsObject
import play.api.mvc.{Action, AnyContent, InjectedController, Request, Result}

import scala.concurrent.{ExecutionContext, Future}

class FormatterUtils @Inject()(
    ec: ExecutionContext,
) extends InjectedController {
  private implicit val iec: ExecutionContext = ec

  trait Resultable[A] {
    def result(a: A): Result
  }
  object Resultable {
    implicit def writableEv[A: Writeable]: Resultable[A] = Ok(_)
    implicit val anyEv: Resultable[Any] = (_: Any) => NoContent
    implicit val streamResultEv: Resultable[StreamResult] = sr => {
      val source = Source
          .fromPublisher(IterateeStreams enumeratorToPublisher Enumerator.fromStream(sr.inputStream))
          .map(ByteString.apply)
      Status(sr.status)
          .sendEntity(HttpEntity.Streamed(source, Some(sr.contentLength), Some(sr.mimeType)))
          .withHeaders(sr.headers.toSeq: _*)
    }
  }

  def ok[C: Writeable](f: Future[C]): Action[AnyContent] = Action.async(f.map(Ok(_)))
  def parse[C: Resultable, A](
      requestParser: Request[AnyContent] => A)(f: A => Future[C]): Action[AnyContent] = Action.async {request =>
    f(requestParser(request)).map(implicitly[Resultable[C]].result)
  }
  def parseText[C: Resultable](f: String => Future[C]): Action[AnyContent] = parse(_.body.asText.get)(f)
  def parseJson[C: Resultable](f: JsObject => Future[C]): Action[AnyContent] =
    parse(_.body.asJson.get.asInstanceOf[JsObject])(f)
}
