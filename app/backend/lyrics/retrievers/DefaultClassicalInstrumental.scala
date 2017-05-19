package backend.lyrics.retrievers

import java.io.File
import backend.Url
import backend.lyrics.Instrumental
import models.Song
import scala.annotation.tailrec
import scala.concurrent.Future

private[lyrics] object DefaultClassicalInstrumental extends LyricsRetriever {
  override def doesUrlMatchHost(url: Url) = false
  override def find(s: Song) = {
    @tailrec
    def isClassical(f: File): Boolean = f.getParentFile match {
      case null => false
      case e => if (e.getName == "Classical") true else isClassical(f.getParentFile)
    }
    if (isClassical(s.file))
      Future.successful(Instrumental("Default"))
    else
      Future.failed(new IllegalArgumentException("Not a classical song"))
  }
  override def parse(url: Url, s: Song) =
    Future.failed(new UnsupportedOperationException("NoUrls"))
}
