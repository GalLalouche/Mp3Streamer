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
  private val webSocket = webSocketFactory(RecentModule.WebSocketName)

  def recent(amount: Int) = converter.ok($.all(amount))
  def double(amount: Int) = converter.ok($.double(amount))
  def last = recent(1)
  def debugLast() = converter.ok($.debugLast())
  def since(dayString: String) = {
    val number = dayString.takeWhile(_.isDigit).toInt
    val last = dayString.last.toLower
    converter.ok(
      if (last == 'd' || last.isDigit)
        $.sinceDays(number)
      else {
        require(last == 'm', "Formats support are pure numbers, numbers ending in d, or ending in m")
        $.sinceMonths(number)
      })
  }

  $.register(webSocket)
  def accept(): WebSocket = webSocket.accept()
}
