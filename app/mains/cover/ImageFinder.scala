package mains.cover

import backend.Url
import backend.configs.Configuration
import common.RichJson._
import common.io.InternetTalker
import net.codingwell.scalaguice.InjectorExtensions._
import org.jsoup.nodes.{Document, Element}
import play.api.libs.json.Json

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

private class ImageFinder(implicit c: Configuration) {
  private implicit val ec: ExecutionContext = c.injector.instance[ExecutionContext]
  private implicit val it: InternetTalker = c.injector.instance[InternetTalker]
  def find(url: Url): Future[Seq[UrlSource]] = it downloadDocument url map parse

  private def parse(d: Document): Seq[UrlSource] =
    d.select("div.rg_meta.notranslate").asScala map toSource
  private def toSource(e: Element): UrlSource = {
    val json = Json.parse(e.html())
    UrlSource(Url(json str "ou"), width = json int "ow", height = json int "oh")
  }
}
