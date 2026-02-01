package musicfinder

import common.path.ref.DirectoryRef
import common.path.ref.io.IOFile

final class IOSongFileFinder extends SongFileFinder {
  override def getSongFilesInDir(d: DirectoryRef): Iterator[IOFile] =
    super.getSongFilesInDir(d).asInstanceOf[Iterator[IOFile]]
}
