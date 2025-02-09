package backend.recent

import javax.inject.Inject

import controllers.PlayActionConverter
import play.api.mvc.InjectedController

import scala.concurrent.ExecutionContext

class RecentController @Inject() (
    $ : RecentFormatter,
    converter: PlayActionConverter,
    ec: ExecutionContext,
) extends InjectedController {
  private implicit val iec: ExecutionContext = ec

  def recent(amount: Int) = converter.ok($.all(amount))
  def double(amount: Int) = converter.ok($.double(amount))
  def last = converter.ok($.last)
  def since(dayString: String) = converter.ok($.since(dayString))
}
