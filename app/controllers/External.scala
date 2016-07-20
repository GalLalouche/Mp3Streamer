package controllers

import backend.external.{ExternalLink, ExternalLinkProvider, ExternalLinksProvider}
import backend.mb.{MbArtistReconcilerCacher, MbExternalLinksProvider, MbHtmlLinkExtractor}
import backend.recon.ArtistReconcilerCacher
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.libs.json.Json.JsValueWrapper
import play.api.mvc.{Action, Controller}

import scala.concurrent.Future

object External extends Controller {
  private val external: ExternalLinksProvider = new MbExternalLinksProvider()

  private val set = Set("allmusic", "wikipedia", "lastfm")
  private def toJson(e: ExternalLink): (String, JsValueWrapper) = e.host.name -> e.link.address

  def get(path: String) = Action.async {
    Future(???)
//    val x= external(Utils.parseSong(path));
//    x.
//        .map(_.artistLinks)
//      .map(_.filter(e => set.contains(e.host.name)))
//      .map(_.map(toJson).toSeq)
//      .map(Json.obj)
//      .map(Ok(_))
  }
}
