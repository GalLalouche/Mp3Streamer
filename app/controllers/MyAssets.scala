package controllers

import java.io.File

import net.codingwell.scalaguice.InjectorExtensions._
import play.api.mvc.Action

import scala.concurrent.ExecutionContext

// Since 2.6 ruined their own assets controller :\
object MyAssets extends LegacyController {
  private implicit val ec: ExecutionContext = c.injector.instance[ExecutionContext]
  def asset(path: String) = Action {
    Ok.sendFile(new File("""public\""" + path))
  }
  def javascript(path: String) = asset("javascripts" + path)
}
