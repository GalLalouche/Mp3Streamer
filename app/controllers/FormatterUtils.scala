package controllers

import javax.inject.Inject
import play.api.http.Writeable
import play.api.mvc.{Action, AnyContent, InjectedController, Request}

import scala.concurrent.{ExecutionContext, Future}

class FormatterUtils @Inject()(
    ec: ExecutionContext,
) extends InjectedController {
  private implicit val iec: ExecutionContext = ec
  def ok[C: Writeable](f: Future[C]): Action[AnyContent] = Action.async(f.map(Ok(_)))
  def parse[C: Writeable, A](
      requestParser: Request[AnyContent] => A)(f: A => Future[C]): Action[AnyContent] = Action.async {request =>
    f(requestParser(request)).map(Ok(_))
  }
}
