package backend.lyrics.retrievers

import backend.module.TestModuleConfiguration
import common.io.{MemoryDir, MemoryRoot}
import common.rich.RichFuture._
import models.{FakeModelFactory, Song}
import net.codingwell.scalaguice.InjectorExtensions._

import scala.concurrent.ExecutionContext

class DefaultClassicalInstrumentalTest extends LyricsSpec {
  private val fakeModelFactory = new FakeModelFactory
  private implicit val ec: ExecutionContext = TestModuleConfiguration().injector.instance[ExecutionContext]
  private val $ = DefaultClassicalInstrumental
  private def songWithPath(path: String): Song = {
    val split = path.split("/")
    val fileName = split.last
    val file = split.dropRight(1).foldLeft(new MemoryRoot: MemoryDir)(_ addSubDir _).addFile(fileName)
    fakeModelFactory.song().copy(file = file)
  }

  private val classicalSong = songWithPath(
    """D:/Media/Music/Classical/Glenn Gould/1955 Goldberg Variations/01 - Aria.flac""")

  "Classical file" in {
    $(classicalSong).get should be a retrievedInstrumental
  }

  "Non classical file" in {
    val nonClassicalSong = songWithPath(
      """D:/Media/Music/Rock/Pop/My Lame Band/01 - My crappy pop song.mp3""")
    $(nonClassicalSong).get shouldReturn RetrievedLyricsResult.NoLyrics
  }
}
