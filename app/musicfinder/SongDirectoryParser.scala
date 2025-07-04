package musicfinder

import com.google.inject.Inject
import models.{Song, SongTagParser}

import common.ds.Types.ViewSeq
import common.io.DirectoryRef

class SongDirectoryParser @Inject() (mf: MusicFinder, parser: SongTagParser) {
  def apply(d: DirectoryRef): ViewSeq[Song] = mf.getSongFilesInDir(d).view.map(parser.apply)
}
