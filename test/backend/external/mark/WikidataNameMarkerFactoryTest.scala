package backend.external.mark

import backend.external.DocumentSpecs
import org.scalatest.FreeSpec

class WikidataNameMarkerFactoryTest extends FreeSpec with DocumentSpecs {
  "basic" in {
    WikidataNameMarkerFactory.extract(getDocument("wikidata.html")) shouldReturn
      "The River (1980 double studio album by Bruce Springsteen)"
  }

  "new version" in {
    WikidataNameMarkerFactory.extract(getDocument("wikidata_duplicates.html")) shouldReturn
      "Time II (2024 studio album by Wintersun)"
  }
}
