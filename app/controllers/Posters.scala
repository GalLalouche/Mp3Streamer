package controllers

import javax.inject.Inject

import play.api.mvc.InjectedController

import scala.concurrent.ExecutionContext

class Posters @Inject() (
    ec: ExecutionContext,
    $ : PostersFormatter,
    decoder: UrlDecodeUtils,
) extends InjectedController {
  private implicit val iec: ExecutionContext = ec

  def image(path: String) = Action(Ok.sendFile($.image(decoder.decode(path))))
}
