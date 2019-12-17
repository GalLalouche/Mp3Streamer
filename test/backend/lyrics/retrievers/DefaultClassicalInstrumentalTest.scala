package backend.lyrics.retrievers

import models.{FakeModelFactory, Song}
import org.scalatest.AsyncFreeSpec

import common.io.{MemoryDir, MemoryRoot}

class DefaultClassicalInstrumentalTest extends AsyncFreeSpec with LyricsSpec {
  private val fakeModelFactory = new FakeModelFactory
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
    $(classicalSong).map(_ should be a retrievedInstrumental)
  }

  "Non classical file" in {
    val nonClassicalSong = songWithPath(
      """D:/Media/Music/Rock/Pop/My Lame Band/01 - My crappy pop song.mp3""")
    $(nonClassicalSong).map(_ shouldReturn RetrievedLyricsResult.NoLyrics)
  }
}
