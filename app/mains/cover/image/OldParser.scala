package mains.cover.image

import backend.Url
import mains.cover.UrlSource
import org.jsoup.nodes.{Document, Element}
import play.api.libs.json.Json

import scala.collection.JavaConverters._

import common.json.RichJson._
import common.rich.RichT._

private object OldParser extends HtmlParser {
  override def apply(d: Document): Option[Seq[UrlSource]] =
    d.select("div.rg_meta.notranslate").asScala.optFilter(_.nonEmpty).map(_.map(toSource))
  private def toSource(e: Element): UrlSource = {
    val json = Json.parse(e.html)
    UrlSource(Url(json str "ou"), width = json int "ow", height = json int "oh")
  }
}
