package backend.external.expansions

import backend.Url
import backend.external.{BaseLink, Host}
import backend.recon.Reconcilable
import com.google.common.annotations.VisibleForTesting
import javax.inject.Inject
import org.jsoup.nodes.Document

import scala.collection.JavaConverters._

import common.rich.collections.RichIterable._

private class WikidataEnglishOrHebrewExtenderFactory @Inject()(helper: ExternalLinkExpanderHelper) {
  def create[R <: Reconcilable]: ExternalLinkExpander[R] = new ExternalLinkExpander[R] {
    override def sourceHost: Host = Host.Wikidata
    override def potentialHostsExtracted: Traversable[Host] = Vector(Host.Wikipedia)
    override def expand = helper(WikidataEnglishOrHebrewExtenderFactory.parse)
  }
}

private object WikidataEnglishOrHebrewExtenderFactory {
  @VisibleForTesting
  def parse[R <: Reconcilable](d: Document) = {
    def selectLanguage(lang: String) =
      d.select(s"""div[data-wb-sitelinks-group="wikipedia"] li[data-wb-siteid="${lang}wiki"] a[hreflang="$lang"]""")
          .asScala
          .ensuring(_ hasAtMostSizeOf 1)
          .headOption
    selectLanguage("en").orElse(selectLanguage("he"))
        .map(e => BaseLink[R](Url(e.attr("href")), Host.Wikipedia))
        .toVector
  }
}
