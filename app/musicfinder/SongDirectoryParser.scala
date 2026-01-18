package musicfinder

import com.google.inject.Inject
import models.{Song, SongTagParser}

import common.io.{DirectoryRef, FileRef}

class SongDirectoryParser @Inject() (sff: SongFileFinder, parser: SongTagParser)
    extends SongTagParser {
  override def apply(file: FileRef): Song = parser(file)
  def apply(d: DirectoryRef): Iterator[Song] = sff.getSongFilesInDir(d).map(parser.apply)
}
