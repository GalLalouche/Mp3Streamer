package backend.external.mark

import backend.external.DocumentSpecs
import org.scalatest.FreeSpec

class WikidataNameMarkerFactoryTest extends FreeSpec with DocumentSpecs {
  WikidataNameMarkerFactory.extract(getDocument("wikidata.html")) shouldReturn
    "The River (1980 double studio album by Bruce Springsteen)"
}
