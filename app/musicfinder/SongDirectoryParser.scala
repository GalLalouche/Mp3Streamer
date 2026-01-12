package musicfinder

import com.google.inject.Inject
import models.{Song, SongTagParser}

import common.io.{DirectoryRef, FileRef}

class SongDirectoryParser @Inject() (mf: MusicFinder, parser: SongTagParser) extends SongTagParser {
  override def apply(file: FileRef): Song = parser(file)
  def apply(d: DirectoryRef): Iterator[Song] = mf.getSongFilesInDir(d).map(parser.apply)
}
