package controllers

import backend.external.{ExternalLink, ExternalLinksProvider}
import backend.mb.MbHtmlLinkExtractor
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.libs.json.Json.JsValueWrapper
import play.api.mvc.{Action, Controller}

object External extends Controller {
  private val external: ExternalLinksProvider = new MbHtmlLinkExtractor

  private val set = Set("allmusic", "wikipedia", "lastfm")
  private def toJson(e: ExternalLink): (String, JsValueWrapper) = e.host.name -> e.link.address

  def get(path: String) = Action.async {
    external.getExternalLinks(Utils.parseSong(path).artistName)
        .map(_.filter (e => set.contains(e.host.name)))
        .map(_.map(toJson).toSeq)
        .map(Json.obj)
        .map(Ok(_))
  }
}
