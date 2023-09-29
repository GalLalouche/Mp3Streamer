package backend.recent

import controllers.websockets.PlayWebSocketRegistryFactory
import controllers.PlayActionConverter
import javax.inject.Inject
import play.api.mvc.{InjectedController, WebSocket}

import scala.concurrent.ExecutionContext

class RecentController @Inject()(
    $: RecentFormatter,
    converter: PlayActionConverter,
    webSocketFactory: PlayWebSocketRegistryFactory,
    ec: ExecutionContext
) extends InjectedController {
  private implicit val iec: ExecutionContext = ec
  private val webSocket = webSocketFactory(RecentModule.WebSocketName)

  def recent(amount: Int) = converter.ok($.all(amount))
  def double(amount: Int) = converter.ok($.double(amount))
  def last = converter.ok($.last)
  def debugLast() = converter.ok($.debugLast())
  def since(dayString: String) = converter.ok($.since(dayString))
  def update(): Unit = $.last.foreach(webSocket broadcast _.toString)

  $.register(webSocket)
  def accept(): WebSocket = {
    val result = webSocket.accept()
    update()
    result
  }
}
