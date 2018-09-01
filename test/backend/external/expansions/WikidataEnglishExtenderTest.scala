package backend.external.expansions

import backend.Url
import backend.configs.TestModuleConfiguration
import backend.external.{BaseLink, DocumentSpecs, Host}
import backend.recon.Artist
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.FreeSpec

class WikidataEnglishExtenderTest extends FreeSpec with DocumentSpecs {
  private val config = TestModuleConfiguration().copy(_urlToBytesMapper = PartialFunction(getBytes))
  private val $ = config.injector.instance[WikidataEnglishExtenderFactory].create[Artist]
  "extract english links if they exist" in {
    $.parseDocument(getDocument("wikidata.htm")) shouldReturn
        Seq(BaseLink(Url("https://en.wikipedia.org/wiki/Bruce_Springsteen"), Host.Wikipedia))
  }
  "return empty when there is no link" in {
    $.parseDocument(getDocument("wikidata_no_english.htm")) shouldReturn Nil
  }
}
