package controllers
import common.Debug
import common.concurrency.ProgressObservable
import play.api.mvc.Action
import rx.lang.scala.Observer
import search.MetadataCacher
import websockets.WebSocketController

/** used for running manual commands from the client side */
object Debugger extends WebSocketController with Debug {
  private def update(msg: String) {
    out._2.push(msg)
  }
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
    extra.!.apply(Observer.apply(update, () => update("complete")))
    Ok(views.html.refresh())
  }
}
