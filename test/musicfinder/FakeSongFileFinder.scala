package musicfinder

import common.io.{DirectoryRef, MemoryFile}

object FakeSongFileFinder extends SongFileFinder {
  override def getSongFilesInDir(d: DirectoryRef): Iterator[MemoryFile] =
    super.getSongFilesInDir(d).asInstanceOf[Iterator[MemoryFile]]
}
