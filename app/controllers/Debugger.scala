package controllers
import common.Debug
import common.concurrency.ProgressObservable
import play.api.mvc.Action
import search.MetadataCacher
import websockets.WebSocketController

/** used for running manual commands from the client side */
object Debugger extends WebSocketController with Debug {
  val extra = new ProgressObservable {
    override def apply(listener: String => Unit) {
      Thread.sleep(1000)
      listener("Starting")
      timed("parsing all files") {
        MetadataCacher.indexAll(listener)
        Searcher.!
      }
      Player.updateMusic()
    }
  }
  def forceRefresh() = Action {
    extra ! safePush
    Ok(views.html.refresh())
  }
}
