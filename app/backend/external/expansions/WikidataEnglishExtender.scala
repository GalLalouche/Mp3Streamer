package backend.external.expansions

import backend.Url
import backend.external.{BaseLink, BaseLinks, Host}
import backend.recon.Reconcilable
import common.io.InternetTalker
import org.jsoup.nodes.Document

import scala.collection.JavaConverters._

// TODO this really shouldn't be generic, but ~> polymorphic
private class WikidataEnglishExtender[R <: Reconcilable](implicit it: InternetTalker)
    extends ExternalLinkExpanderTemplate[R](Host.Wikidata, List(Host.Wikipedia)) {
  override def parseDocument(d: Document): BaseLinks[R] =
    d.select("""div[data-wb-sitelinks-group="wikipedia"] li[data-wb-siteid="enwiki"] a[hreflang="en"]""")
      .asScala
      .ensuring(_.size <= 1)
      .headOption
      .map(e => BaseLink[R](Url(e.attr("href")), Host.Wikipedia))
      .toList
}
