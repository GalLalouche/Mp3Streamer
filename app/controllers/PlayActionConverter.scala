package controllers

import javax.inject.Inject

import akka.stream.scaladsl.Source
import akka.util.ByteString
import play.api.http.{HttpEntity, Writeable}
import play.api.libs.iteratee.Enumerator
import play.api.libs.iteratee.streams.IterateeStreams
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent, InjectedController, Request, Result}

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.BetterFutureInstances._
import common.rich.func.ToMoreMonadErrorOps._
import scalaz.Scalaz.ToIdOps
import scalaz.syntax.functor.ToFunctorOps

/** Converts common play-agnostic return values, usually from formatter helpers, to play Actions. */
class PlayActionConverter @Inject() (
    ec: ExecutionContext,
    assets: Assets,
) extends InjectedController {
  private implicit val iec: ExecutionContext = ec
  def ok[C: Writeable](f: Future[C]): Action[AnyContent] =
    Action.async(f.map(Ok(_)).|>(handleFutureFailure))
  def ok[C: Writeable](c: C): Action[AnyContent] = Action(Ok(c))

  def redirect(url: Future[String]): Action[AnyContent] =
    Action.async(url.map(Redirect(_)).|>(handleFutureFailure))

  private def handleFutureFailure(f: Future[Result]): Future[Result] =
    f.listenError(_.printStackTrace()).handleErrorFlat(InternalServerError apply _.getMessage)

  def noContent(f: Future[Any]): Action[AnyContent] = Action.async(f >| NoContent)
  def noContent(a: Any): Action[AnyContent] = Action(NoContent)

  trait Resultable[A] {
    def result(a: A): Result
  }
  object Resultable {
    implicit def writableEv[A: Writeable]: Resultable[A] = Ok(_)
    implicit val anyEv: Resultable[Any] = (_: Any) => NoContent
    implicit val streamResultEv: Resultable[StreamResult] = sr => {
      val source = Source
        .fromPublisher(IterateeStreams.enumeratorToPublisher(Enumerator.fromStream(sr.inputStream)))
        .map(ByteString.apply)
      Status(sr.status)
        .sendEntity(HttpEntity.Streamed(source, Some(sr.contentLength), Some(sr.mimeType)))
        .withHeaders(sr.headers.toSeq: _*)
    }
    implicit val resultEv: Resultable[Result] = e => e
  }
  private implicit class ResultableOps[R: Resultable]($ : R) {
    def result: Result = implicitly[Resultable[R]].result($)
  }

  trait Actionable[A] {
    def apply(f: Request[AnyContent] => A): Action[AnyContent]
  }
  object Actionable {
    implicit def syncEv[A: Resultable]: Actionable[A] = f => Action(f(_).result)
    implicit def asyncEv[A: Resultable]: Actionable[Future[A]] = f =>
      Action.async(
        f(_)
          .map(_.result)
          .|>(handleFutureFailure),
      )
  }

  class _Parser[A] private[PlayActionConverter] (parse: Request[AnyContent] => A) {
    def apply[C: Actionable](f: A => C): Action[AnyContent] =
      implicitly[Actionable[C]].apply(parse andThen f)
  }

  def parse[A](parser: Request[AnyContent] => A): _Parser[A] = new _Parser[A](parser)
  def parseText: _Parser[String] = parse(_.body.asText.get)
  def parseJson: _Parser[JsValue] = parse(_.body.asJson.get)

  def html(fileName: String) = assets.at("/public/html", fileName + ".html")
}
