package backend.recent

import controllers.websockets.PlayWebSocketRegistryFactory
import controllers.PlayActionConverter
import javax.inject.Inject
import play.api.mvc.{InjectedController, WebSocket}

class RecentController @Inject()(
    $: RecentFormatter,
    converter: PlayActionConverter,
    webSocketFactory: PlayWebSocketRegistryFactory,
) extends InjectedController {
  private val webSocket = webSocketFactory("Recent")

  def recent(amount: Int) = converter.ok($.all(amount))
  def double(amount: Int) = converter.ok($.double(amount))
  def last = recent(1)

  $.register(webSocket)
  def accept(): WebSocket = webSocket.accept()
}
