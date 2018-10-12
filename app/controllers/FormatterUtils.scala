package controllers

import akka.stream.scaladsl.Source
import akka.util.ByteString
import javax.inject.Inject
import play.api.http.{HttpEntity, Writeable}
import play.api.libs.iteratee.streams.IterateeStreams
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent, InjectedController, Request, Result}

import scala.concurrent.{ExecutionContext, Future}

class FormatterUtils @Inject()(
    ec: ExecutionContext,
) extends InjectedController {
  private implicit val iec: ExecutionContext = ec

  def ok[C: Writeable](f: Future[C]): Action[AnyContent] = Action.async(f.map(Ok(_)))
  def ok[C: Writeable](c: C): Action[AnyContent] = Action(Ok(c))

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
  private implicit class ResultableOps[R: Resultable]($: R) {
    def result: Result = implicitly[Resultable[R]].result($)
  }

  trait Actionable[A] {
    def apply(f: Request[AnyContent] => A): Action[AnyContent]
  }
  object Actionable {
    implicit def syncEv[A: Resultable]: Actionable[A] = f => Action(f(_).result)
    implicit def asyncEv[A: Resultable]: Actionable[Future[A]] = f => Action.async(f(_).map(_.result))
  }

  class _Parser[A] private[FormatterUtils](parse: Request[AnyContent] => A) {
    def apply[C: Actionable](f: A => C): Action[AnyContent] = implicitly[Actionable[C]].apply(parse andThen f)
  }

  def parse[A](parser: Request[AnyContent] => A): _Parser[A] = new _Parser[A](parser)
  def parseText: _Parser[String] = parse(_.body.asText.get)
  def parseJson: _Parser[JsValue] = parse(_.body.asJson.get)
}
