package backend.external.expansions

import backend.Url
import backend.configs.TestConfiguration
import backend.external.{BaseLink, DocumentSpecs, Host}
import backend.recon.Artist
import org.scalatest.FreeSpec

class WikidataEnglishExtenderTest extends FreeSpec with DocumentSpecs {
  private implicit val config: TestConfiguration =
    TestConfiguration().copy(_urlToBytesMapper = PartialFunction(getBytes))
  private val $ = new WikidataEnglishExtender[Artist]
  "extract english links if they exist" in {
    $.parseDocument(getDocument("wikidata.htm")) shouldReturn
        Seq(BaseLink(Url("https://en.wikipedia.org/wiki/Bruce_Springsteen"), Host.Wikipedia))
  }
  "return empty when there is no link" in {
    $.parseDocument(getDocument("wikidata_no_english.htm")) shouldReturn Nil
  }
}
