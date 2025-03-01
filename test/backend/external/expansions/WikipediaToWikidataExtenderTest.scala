package backend.external.expansions

import backend.external.{BaseLink, DocumentSpecs, Host}
import backend.module.TestModuleConfiguration
import io.lemonlabs.uri.Url
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.FreeSpec

class WikipediaToWikidataExtenderTest extends FreeSpec with DocumentSpecs {
  private val config = TestModuleConfiguration().copy(_urlToBytesMapper = { case x => getBytes(x) })
  private val $ = config.injector.instance[WikipediaToWikidataExtenderFactory]
  "Extracts wikidata item" in {
    $.parse(getDocument("wikipedia-discography.html")) shouldReturn
      Vector(BaseLink(Url.parse("https://www.wikidata.org/wiki/Q15982430"), Host.Wikidata))
  }
}
