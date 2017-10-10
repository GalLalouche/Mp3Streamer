package controllers

import java.io.File

import play.api.mvc.Action

// Since 2.6 ruined their own assets controller :\
object MyAssets extends LegacyController {
  def asset(path: String) = Action {
    Ok.sendFile(new File("""public\""" + path))
  }
  def javascript(path: String) = asset("javascripts" + path)
}
