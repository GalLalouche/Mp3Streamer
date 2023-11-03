package controllers

import java.io.File
import javax.inject.Inject
import scala.concurrent.ExecutionContext

import play.api.mvc.InjectedController

// Since 2.6 ruined their own assets controller :\
class MyAssets @Inject() (implicit ec: ExecutionContext) extends InjectedController {
  def asset(path: String) = Action {
    Ok.sendFile(new File("""public\""" + path))
  }
  def javascript(path: String) = asset("javascripts" + path)
}
