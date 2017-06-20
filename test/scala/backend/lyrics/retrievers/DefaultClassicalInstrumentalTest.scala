package backend.lyrics.retrievers

import backend.Url
import backend.configs.TestConfiguration
import backend.lyrics.Instrumental
import common.rich.RichFuture._
import org.scalatest.{FreeSpec, ShouldMatchers}
import search.FakeModelFactory

class DefaultClassicalInstrumentalTest extends FreeSpec with ShouldMatchers {
  private implicit val tc = TestConfiguration()
  private val $ = DefaultClassicalInstrumental
  private val classicalSong = FakeModelFactory.mockSong(file="""D:/Media/Music/Classical/Glenn Gould/1955 Goldberg Variations/01 - Aria.flac""")

  "Classical file" in {
    $.find(classicalSong).get shouldBe an[Instrumental]
  }

  "Non classical file" in {
    val song = FakeModelFactory.mockSong(file="""D:/Media/Music/Rock/Pop/My Lame Band/01 - My crappy pop song.mp3""")
    $.find(song).getFailure should not be null
  }

  "Parse URL should fail" - {
    "doesUrlMatchHost" in {
      $.parse(Url("Some url"), classicalSong).getFailure should not be null
    }
  }
}
