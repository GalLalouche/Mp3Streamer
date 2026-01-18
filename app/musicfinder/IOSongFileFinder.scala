package musicfinder

import common.io.{DirectoryRef, IOFile}

final class IOSongFileFinder extends SongFileFinder {
  override def getSongFilesInDir(d: DirectoryRef): Iterator[IOFile] =
    super.getSongFilesInDir(d).asInstanceOf[Iterator[IOFile]]
}
