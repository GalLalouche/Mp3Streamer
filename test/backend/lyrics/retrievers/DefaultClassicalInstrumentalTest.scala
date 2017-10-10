package backend.lyrics.retrievers

import backend.configs.TestConfiguration
import backend.lyrics.Instrumental
import common.io.{MemoryDir, MemoryRoot}
import common.rich.RichFuture._
import models.{FakeModelFactory, Song}
import org.scalatest.{FreeSpec, Matchers}

class DefaultClassicalInstrumentalTest extends FreeSpec with Matchers {
  private val fakeModelFactory = new FakeModelFactory
  private implicit val tc = TestConfiguration()
  private val $ = DefaultClassicalInstrumental
  private def songWithPath(path: String): Song = {
    val split = path.split("/")
    val filePath = split.last
    val file = split.dropRight(1)
        ./:(new MemoryRoot: MemoryDir)((dir, name) => dir addSubDir name)
        .addFile(filePath)
    val $ = fakeModelFactory.song()
    $.copy(file = file)
  }

  private val classicalSong = songWithPath(
    """D:/Media/Music/Classical/Glenn Gould/1955 Goldberg Variations/01 - Aria.flac""")

  "Classical file" in {
    $(classicalSong).get shouldBe an[Instrumental]
  }

  "Non classical file" in {
    val nonClassicalSong = songWithPath(
      """D:/Media/Music/Rock/Pop/My Lame Band/01 - My crappy pop song.mp3""")
    $(nonClassicalSong).getFailure should not be null
  }
}
