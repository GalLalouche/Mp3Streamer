package musicfinder

import common.path.ref.DirectoryRef
import common.test.memory_ref.MemoryFile

object FakeSongFileFinder extends SongFileFinder {
  override def getSongFilesInDir(d: DirectoryRef): Iterator[MemoryFile] =
    super.getSongFilesInDir(d).asInstanceOf[Iterator[MemoryFile]]
}
