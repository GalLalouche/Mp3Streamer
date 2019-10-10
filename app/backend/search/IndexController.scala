package backend.search

import controllers.websockets.PlayWebSocketRegistryFactory
import controllers.PlayActionConverter
import controllers.websockets.WebSocketRef.AsyncWebSocketRefReader
import javax.inject.Inject
import play.api.mvc.{InjectedController, WebSocket}

/** Used for updating the search index from the client. */
class IndexController @Inject()(
    $: IndexFormatter,
    converter: PlayActionConverter,
    webSocketFactory: PlayWebSocketRegistryFactory,
) extends InjectedController {
  private val webSocket = webSocketFactory("IndexController")
  private def run(wsReader: AsyncWebSocketRefReader) = converter.ok {
    wsReader.run(webSocket)
    views.html.refresh()
  }

  def cacheAll() = run($.cacheAll())
  def quickRefresh() = run($.quickRefresh())
  def accept(): WebSocket = webSocket.accept()
}
