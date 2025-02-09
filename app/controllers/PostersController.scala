package controllers

import javax.inject.Inject

import play.api.mvc.InjectedController

import scala.concurrent.ExecutionContext

import common.rich.func.ToMoreFoldableOps.toMoreFoldableOps
import scalaz.Scalaz.optionInstance

class PostersController @Inject() (
    ec: ExecutionContext,
    $ : PostersFormatter,
) extends InjectedController {
  private implicit val iec: ExecutionContext = ec

  def image(path: String) = Action(
    $.image(PlayUrlDecoder(path)).mapHeadOrElse(Ok.sendFile(_), NotFound(path + " Does not exist")),
  )
}
