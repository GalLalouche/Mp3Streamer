package mains.vimtag.lines

import mains.vimtag.{IndividualId3, IndividualParser, Tags}

import common.rich.collections.RichSeq._
import common.rich.primitives.RichBoolean.richBoolean
import common.rich.primitives.RichOption._

private object LinesParser extends IndividualParser {
  private def toMaps(lines: Seq[String]): Seq[Map[String, String]] =
    lines.cutoffsAt(_ startsWith Tags.File).map(splitMap)
  private def individual(map: Map[String, String]): IndividualId3 = {
    val file = map(Tags.File)
    def orThrow(tag: String): String =
      map.get(tag).getOrThrow(new NoSuchElementException(s"key not found for $file: $tag"))
    IndividualId3(
      relativeFileName = file,
      title = orThrow(Tags.Title),
      track = orThrow(Tags.Track).toInt,
      discNumber = map.get(Tags.DiscNo).filter(_.nonEmpty),
    )
  }
  override def cutoff = _.startsWith("FILE").isFalse
  override def apply(xs: Seq[String]) = toMaps(xs).map(individual)
}
