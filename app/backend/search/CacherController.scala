package backend.search

import common.json.ToJsonableOps
import controllers.websockets.PlayWebSocketRegistryFactory
import controllers.PlayActionConverter
import controllers.websockets.WebSocketRef.WebSocketRefReader
import javax.inject.Inject
import play.api.mvc.{InjectedController, WebSocket}

/** Used for updating the cache from the client. */
class CacherController @Inject()(
    $: CacherFormatter,
    converter: PlayActionConverter,
    webSocketFactory: PlayWebSocketRegistryFactory,
) extends InjectedController with ToJsonableOps {
  private val webSocket = webSocketFactory("CacherController")
  private def run(wsReader: WebSocketRefReader) = converter.ok {
    wsReader.run(webSocket)
    views.html.refresh()
  }

  def forceRefresh() = run($.forceRefresh())
  def quickRefresh() = run($.quickRefresh())
  def accept(): WebSocket = webSocket.accept()
}
