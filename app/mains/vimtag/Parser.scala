package mains.vimtag

import java.io.File

import scalaz.std.tuple.tuple2Bitraverse
import scalaz.syntax.bifunctor.ToBifunctorOps

import common.rich.RichT._
import common.rich.collections.RichSeq._
import common.rich.primitives.RichBoolean._
import common.rich.primitives.RichOption._

private object Parser {
  private def splitMap(lines: Seq[String]): Map[String, String] =
    lines.flatMap(_.trim.split(": ?", 2).opt.filter(_.length == 2).map(_.toTuple(_ (0), _ (1)))).toMap
  private def toMaps(lines: Seq[String]): Seq[Map[String, String]] =
    lines.cutoffsAt(_ startsWith Tags.File).map(splitMap)
  private def individual(map: Map[String, String]): IndividualId3 = {
    val file = new File(map(Tags.File))
    def orThrow(tag: String): String =
      map.get(tag).getOrThrow(new NoSuchElementException(s"key not found for $file: $tag"))
    IndividualId3(
      file = file,
      title = orThrow(Tags.Title),
      track = orThrow(Tags.Track).toInt,
      discNumber = map.get(Tags.DiscNo).filter(_.nonEmpty),
    )
  }

  def apply(lines: Seq[String]): ParsedId3 = {
    val (globals: Map[String, String], individuals: Seq[Map[String, String]]) =
      lines.span(_.startsWith(Tags.File).isFalse).bimap(splitMap, toMaps)
    def required(key: String): RequiredTag[String] = RequiredTag(globals(key))
    def optional(tag: String): ParsedTag[String] =
      globals.get(tag).filter(_.nonEmpty).map(RequiredTag.apply).getOrElse(Empty)
    ParsedId3(
      artist = required(Tags.Artist),
      album = required(Tags.Album),
      year = required(Tags.Year).map[Int](_.toInt),

      composer = optional(Tags.Composer),
      opus = optional(Tags.Opus),
      conductor = optional(Tags.Conductor),
      orchestra = optional(Tags.Orchestra),
      performanceYear = optional(Tags.PerformanceYear).map(_.toInt),

      flags = Flag.parse(lines),

      songId3s = individuals.map(individual),
    )
  }
}
