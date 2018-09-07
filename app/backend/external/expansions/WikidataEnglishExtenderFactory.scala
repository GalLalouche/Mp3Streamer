package backend.external.expansions

import backend.Url
import backend.external.{BaseLink, Host}
import backend.recon.Reconcilable
import com.google.common.annotations.VisibleForTesting
import common.rich.collections.RichSeq._
import javax.inject.Inject
import org.jsoup.nodes.Document

import scala.collection.JavaConverters._

// TODO this really shouldn't be generic, but ~> polymorphic
private class WikidataEnglishExtenderFactory @Inject()(helper: ExternalLinkExpanderHelper) {
  @VisibleForTesting
  def parse[R <: Reconcilable](d: Document) =
    d.select("""div[data-wb-sitelinks-group="wikipedia"] li[data-wb-siteid="enwiki"] a[hreflang="en"]""")
        .asScala
        .ensuring(_ hasAtMostSizeOf 1)
        .headOption
        .map(e => BaseLink[R](Url(e.attr("href")), Host.Wikipedia))
        .toVector
  def create[R <: Reconcilable]: ExternalLinkExpander[R] = new ExternalLinkExpander[R] {
    override def sourceHost: Host = Host.Wikidata
    override def potentialHostsExtracted: Traversable[Host] = Vector(Host.Wikipedia)
    override def expand = helper(parse)
  }
}
