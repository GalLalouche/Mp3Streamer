package backend.external.expansions

import backend.Url
import backend.external.{BaseLink, DocumentSpecs, Host}
import org.scalatest.FreeSpec

import common.rich.collections.RichTraversableOnce._

class WikidataEnglishExtenderFactoryTest extends FreeSpec with DocumentSpecs {
  "parse" - {
    "extract English links if they exist" in {
      WikidataEnglishOrHebrewExtenderFactory.parse(getDocument("wikidata.htm")).single shouldReturn
          BaseLink(Url("https://en.wikipedia.org/wiki/Bruce_Springsteen"), Host.Wikipedia)
    }
    "extract Hebrew links if there is no english link" in {
      WikidataEnglishOrHebrewExtenderFactory.parse(getDocument("hebrew_wikidata.htm")).single shouldReturn
          BaseLink(Url("https://he.wikipedia.org/wiki/%D7%A1%D7%99%D7%9E%D7%A0%D7%99%D7%9D_%D7%A9%D7%9C_%D7%97%D7%95%D7%9C%D7%A9%D7%94"), Host.Wikipedia)
    }
    "return empty when there is no link" in {
      WikidataEnglishOrHebrewExtenderFactory.parse(getDocument("wikidata_no_english.htm")) shouldReturn Vector.empty
    }
  }
}
