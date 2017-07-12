package backend.lyrics.retrievers

import backend.Url
import backend.lyrics.Instrumental
import common.io.DirectoryRef
import models.Song

import scala.annotation.tailrec
import scala.concurrent.Future

private[lyrics] object DefaultClassicalInstrumental extends LyricsRetriever {
  override def doesUrlMatchHost(url: Url) = false
  override def find(s: Song) = {
    @tailrec
    def isClassical(f: DirectoryRef): Boolean =
      f.name == "Classical" || f.name == "New Age" || (f.hasParent && isClassical(f.parent))
    if (isClassical(s.file.parent))
      Future.successful(Instrumental("Default"))
    else
      Future.failed(new IllegalArgumentException("Not a classical song"))
  }
  override def parse(url: Url, s: Song) =
    Future.failed(new UnsupportedOperationException("NoUrls"))
}
