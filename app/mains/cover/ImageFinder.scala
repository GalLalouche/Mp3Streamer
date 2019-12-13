package mains.cover

import backend.Url
import common.json.RichJson._
import common.io.InternetTalker
import javax.inject.Inject
import org.jsoup.nodes.{Document, Element}
import play.api.libs.json.Json

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

private class ImageFinder @Inject()(it: InternetTalker) {
  private implicit val iec: ExecutionContext = it
  def find(url: Url): Future[Seq[UrlSource]] = it downloadDocument url map parse

  private def parse(d: Document): Seq[UrlSource] =
    d.select("div.rg_meta.notranslate").asScala map toSource
  private def toSource(e: Element): UrlSource = {
    val json = Json.parse(e.html())
    UrlSource(Url(json str "ou"), width = json int "ow", height = json int "oh")
  }
}
