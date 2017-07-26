package backend.lyrics.retrievers

import common.io.DirectoryRef
import models.Song

import scala.annotation.tailrec

private[lyrics] object DefaultClassicalInstrumental extends DefaultInstrumental {
  @tailrec
  private def isInstrumental(f: DirectoryRef): Boolean =
    f.name == "Classical" || f.name == "New Age" || (f.hasParent && isInstrumental(f.parent))
  override protected final def isInstrumental(s: Song): Boolean = isInstrumental(s.file.parent)
  override protected val defaultType = "Classical/NewAge"
}
