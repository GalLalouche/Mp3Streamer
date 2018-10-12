package backend.recent

import common.json.ToJsonableOps
import controllers.websockets.PlayWebSocketRegistryFactory
import controllers.PlayActionConverter
import javax.inject.Inject
import play.api.mvc.{InjectedController, WebSocket}

class RecentController @Inject()(
    $: RecentFormatter,
    converter: PlayActionConverter,
    webSocketFactory: PlayWebSocketRegistryFactory,
) extends InjectedController with ToJsonableOps {
  private val webSocket = webSocketFactory("Recent")

  def recent(amount: Int) = converter.ok($.recent(amount))
  def last = recent(1)

  $.register(webSocket)
  def accept(): WebSocket = webSocket.accept()
}
