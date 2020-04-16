package backend.lyrics.retrievers

import scala.annotation.tailrec
import scala.concurrent.Future

import common.io.DirectoryRef

private[lyrics] object DefaultClassicalInstrumental extends LyricsRetriever {
  @tailrec private def isInstrumental(f: DirectoryRef): Boolean =
    f.name == "Classical" || f.name == "New Age" || (f.hasParent && isInstrumental(f.parent))
  private val helper = new DefaultInstrumentalHelper("Classical/NewAge")
  override def get = s => Future.successful(helper(isInstrumental(s.file.parent)))
}
