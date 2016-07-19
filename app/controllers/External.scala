package controllers

import backend.external.{ExternalLink, ExternalLinkProvider}
import backend.mb.{MbArtistReconciler, MbHtmlLinkExtractor}
import backend.recon.ArtistReconciler
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.libs.json.Json.JsValueWrapper
import play.api.mvc.{Action, Controller}

object External extends Controller {
  private val external: ExternalLinkProvider = new MbHtmlLinkExtractor
  private val reconciler: ArtistReconciler = MbArtistReconciler

  private val set = Set("allmusic", "wikipedia", "lastfm")
  private def toJson(e: ExternalLink): (String, JsValueWrapper) = e.host.name -> e.link.address

  def get(path: String) = Action.async {
    reconciler.get(Utils.parseSong(path).artistName)
      .filter(_._1.isDefined)
      .map(_._1.get)
      .flatMap(external.apply)
      .map(_.filter(e => set.contains(e.host.name)))
      .map(_.map(toJson).toSeq)
      .map(Json.obj)
      .map(Ok(_))
  }
}
