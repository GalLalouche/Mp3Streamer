package controllers
import common.{Tree, Debug}
import loggers.{CompositeLogger, ConsoleLogger}
import play.api.mvc.{Action, Controller}
import search.MetadataCacher

/** used for running manual commands from the client side */
object Debugger extends Controller with Debug {
  def forceRefresh() = Action {
    timed("parsing all files") {
      MetadataCacher.indexAll()
    }
    Player.updateMusic()
    Ok("Refreshing! watch console or websockets for updates")
  }
}
