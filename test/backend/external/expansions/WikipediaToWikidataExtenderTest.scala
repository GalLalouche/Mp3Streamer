package backend.external.expansions

import org.scalatest.FreeSpec

import backend.external.{BaseLink, DocumentSpecs, Host}
import backend.module.TestModuleConfiguration
import backend.Url
import net.codingwell.scalaguice.InjectorExtensions._

class WikipediaToWikidataExtenderTest extends FreeSpec with DocumentSpecs {
  private val config = TestModuleConfiguration().copy(_urlToBytesMapper = PartialFunction(getBytes))
  private val $ = config.injector.instance[WikipediaToWikidataExtenderFactory]
  "Extracts wikidata item" in {
    $.parse(getDocument("wikipedia-discography.html")) shouldReturn
      Vector(BaseLink(Url("https://www.wikidata.org/wiki/Q15982430"), Host.Wikidata))
  }
}
