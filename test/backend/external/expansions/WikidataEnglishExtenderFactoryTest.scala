package backend.external.expansions

import backend.Url
import backend.external.{BaseLink, DocumentSpecs, Host}
import org.scalatest.FreeSpec

class WikidataEnglishExtenderFactoryTest extends FreeSpec with DocumentSpecs {
  "parse" - {
    "extract english links if they exist" in {
      WikidataEnglishExtenderFactory.parse(getDocument("wikidata.htm")) shouldReturn
          Vector(BaseLink(Url("https://en.wikipedia.org/wiki/Bruce_Springsteen"), Host.Wikipedia))
    }
    "return empty when there is no link" in {
      WikidataEnglishExtenderFactory.parse(getDocument("wikidata_no_english.htm")) shouldReturn Vector.empty
    }
  }
}
