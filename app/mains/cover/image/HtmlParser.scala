package mains.cover.image

import mains.cover.UrlSource
import org.jsoup.nodes.Document

import common.io.DirectoryRef
import common.rich.collections.RichTraversableOnce._
import common.rich.primitives.RichOption._

private trait HtmlParser {
  def apply(d: Document): Option[Seq[UrlSource]]
}

private object HtmlParser {
  private val dumpPath = "mp3_dump.html"
  // Google search results apparently choose the HTML format randomly, so just try all known parsers until one
  // of them succeeds, i.e., returns a non-empty list of links.
  def composite(dumpDir: DirectoryRef, p1: HtmlParser, ps: HtmlParser*): Document => Seq[UrlSource] = {
    val parsers = p1 :: ps.toList
    document =>
      parsers.mapFirst(_ (document)).getOrThrow {
        val dumpFile = dumpDir.addFile(dumpPath)
        dumpFile.clear().write(document.toString)
        "No parser found images in document; dumping to " + dumpFile.path
      }
  }
}
