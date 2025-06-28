package musicfinder

import com.google.inject.Inject
import models.{Song, SongTagParser}

import common.ds.Types.ViewSeq
import common.io.{DirectoryRef, FileRef}

class SongDirectoryParser @Inject() (mf: MusicFinder, parser: SongTagParser) extends SongTagParser {
  override def apply(file: FileRef): Song = parser(file)
  def apply(d: DirectoryRef): ViewSeq[Song] = mf.getSongFilesInDir(d).view.map(parser.apply)
}
