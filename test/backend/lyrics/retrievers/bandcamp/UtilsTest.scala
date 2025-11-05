package backend.lyrics.retrievers.bandcamp

import common.test.AuxSpecs
import io.lemonlabs.uri.Url
import org.scalatest.freespec.AnyFreeSpec

class UtilsTest extends AnyFreeSpec with AuxSpecs {
  "doesUrlMatchHost" - {
    def matches(s: String) = Utils.doesUrlMatchHost(Url(s))
    "matches" in {
      matches("https://shanipeleg.bandcamp.com/track/--3") shouldReturn true
    }
    "doesn't match" in {
      matches("https://www.musixmatch.com/lyrics/Hayehudim/%D7%92-%D7%A7%D7%99") shouldReturn false
    }
  }
}
