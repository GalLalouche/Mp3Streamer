package backend.external.expansions

import java.util.regex.Pattern

import backend.Url
import backend.external.{BaseLink, Host}
import backend.recon.Reconcilable
import com.google.common.annotations.VisibleForTesting
import javax.inject.Inject
import org.jsoup.nodes.Document

import common.RichJsoup._
import common.rich.primitives.RichString._

private class WikipediaToWikidataExtenderFactory @Inject()(helper: ExternalLinkExpanderHelper) {
  private def canonize(href: String): String = {
    val wikidataId = href.split("/").last
    assert(wikidataId matches WikipediaToWikidataExtenderFactory.WikidataItemIdPattern,
      s"invalid Wikidata ID <$wikidataId>")
    "https://www.wikidata.org/wiki/" + wikidataId
  }
  @VisibleForTesting
  def parse[R <: Reconcilable](d: Document) =
    d.selectSingleOpt("#t-wikibase a")
        .map(e => BaseLink[R](Url(canonize(e.attr("href"))), Host.Wikidata))
        .toVector
  def create[R <: Reconcilable]: ExternalLinkExpander[R] = new ExternalLinkExpander[R] {
    override def sourceHost: Host = Host.Wikipedia
    override def potentialHostsExtracted: Traversable[Host] = Vector(Host.Wikidata)
    override def expand = helper(parse)
  }
}

private object WikipediaToWikidataExtenderFactory {
  private val WikidataItemIdPattern = Pattern compile "Q\\d+"
}
