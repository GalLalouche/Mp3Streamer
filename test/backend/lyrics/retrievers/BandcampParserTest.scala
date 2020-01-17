package backend.lyrics.retrievers

import backend.Url
import models.FakeModelFactory
import org.scalatest.FreeSpec
import org.scalatest.mockito.MockitoSugar

class BandcampParserTest extends FreeSpec with LyricsSpec with MockitoSugar {
  private val fakeModelFactory = new FakeModelFactory
  "doesUrlMatchHost" - {
    val $ = new BandcampParser(mock[SingleHostParsingHelper])
    def matches(s: String) = $ doesUrlMatchHost Url(s)
    "matches" in {
      matches("https://shanipeleg.bandcamp.com/track/--3") shouldReturn true
    }
    "doesn't match" in {
      matches("https://www.musixmatch.com/lyrics/Hayehudim/%D7%92-%D7%A7%D7%99") shouldReturn false
    }
  }
  "parse" - {
    "english" in {
      verifyLyrics(
        BandcampParser.parser(getDocument("bandcamp_english.html"), fakeModelFactory.song()),
        "you can tell",
        "I AM THE GIRL ANACHRONISM",
      )
    }
    "hebrew" in {
      verifyLyrics(
        BandcampParser.parser(getDocument("bandcamp_hebrew.html"), fakeModelFactory.song()),
        "ג'נה לבדה יושבת על הדשא",
        "וכל מה שידעה פתאום נעלם ממנה כמו חלום",
      )
    }
  }
}
