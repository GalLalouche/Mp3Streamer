package controllers

import javax.inject.Inject

import play.api.mvc.InjectedController

import scala.concurrent.ExecutionContext

class PostersController @Inject() (
    ec: ExecutionContext,
    $ : PostersFormatter,
) extends InjectedController {
  private implicit val iec: ExecutionContext = ec

  def image(path: String) = Action(Ok.sendFile($.image(PlayUrlDecoder(path))))
}
