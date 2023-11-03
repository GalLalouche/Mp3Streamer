package backend.external.mark

import org.scalatest.FreeSpec

import backend.external.DocumentSpecs

class WikidataNameMarkerFactoryTest extends FreeSpec with DocumentSpecs {
  WikidataNameMarkerFactory.extract(getDocument("wikidata.html")) shouldReturn
    "The River (1980 double studio album by Bruce Springsteen)"
}
