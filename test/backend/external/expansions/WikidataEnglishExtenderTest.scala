package backend.external.expansions

import backend.Url
import backend.external.{BaseLink, DocumentSpecs, Host}
import backend.module.TestModuleConfiguration
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.FreeSpec

class WikidataEnglishExtenderTest extends FreeSpec with DocumentSpecs {
  private val config = TestModuleConfiguration().copy(_urlToBytesMapper = PartialFunction(getBytes))
  private val $ = config.injector.instance[WikidataEnglishExtenderFactory]
  "extract english links if they exist" in {
    $.parse(getDocument("wikidata.htm")) shouldReturn
        Vector(BaseLink(Url("https://en.wikipedia.org/wiki/Bruce_Springsteen"), Host.Wikipedia))
  }
  "return empty when there is no link" in {
    $.parse(getDocument("wikidata_no_english.htm")) shouldReturn Vector.empty
  }
}
