package backend.external.expansions

import backend.external.{BaseLink, Host}
import backend.recon.Reconcilable
import backend.Url
import com.google.common.annotations.VisibleForTesting
import javax.inject.Inject
import org.jsoup.nodes.Document

import common.RichJsoup._

private class WikidataEnglishOrHebrewExtenderFactory @Inject() (
    helper: ExternalLinkExpanderHelper,
) {
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
      d.selectSingleOpt(
        s"""div[data-wb-sitelinks-group="wikipedia"] li[data-wb-siteid="${lang}wiki"] a[hreflang="$lang"]""",
      )
    selectLanguage("en")
      .orElse(selectLanguage("he"))
      .map(e => BaseLink[R](Url(e.href), Host.Wikipedia))
      .toVector
  }
}
