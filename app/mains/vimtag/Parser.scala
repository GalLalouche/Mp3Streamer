package mains.vimtag

import javax.inject.Inject

import scalaz.std.tuple.tuple2Bitraverse
import scalaz.syntax.bifunctor.ToBifunctorOps

private class Parser @Inject()(aux: IndividualParser) {
  def apply(existingMappings: Map[String, InitialValues])(lines: Seq[String]): ParsedId3 = {
    val (globals: Map[String, String], individuals: Seq[IndividualId3]) = lines
        .filterNot(_ startsWith "#")
        .span(aux.cutoff)
        .bimap(aux.splitMap, aux.apply)
    def required(key: String): RequiredTag[String] = RequiredTag.parser(existingMappings(key))(globals(key))
    def optional(tag: String): ParsedTag[String] =
      globals.get(tag).filter(_.nonEmpty).map(RequiredTag.parser(existingMappings(tag))).getOrElse(Empty)
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

      songId3s = individuals,
    )
  }
}
