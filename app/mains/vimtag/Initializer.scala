package mains.vimtag

import javax.inject.Inject
import mains.vimtag.Initializer.InitialLines
import models.{MusicFinder, OptionalSong}

import common.io.DirectoryRef
import common.rich.RichT._
import common.rich.RichTuple.richTuple2
import common.rich.collections.RichSeq._
import common.rich.primitives.RichBoolean.richBoolean

/** Sets up the initial lines. */
private class Initializer @Inject()(mf: MusicFinder, aux: IndividualInitializer) {
  private class Extractor(dir: DirectoryRef) {
    private lazy val songs = (dir +: dir.deepDirs)
        .flatMap(mf.getOptionalSongsInDir)
        .sortBy(_.file)
    private def headOrKeep(set: Set[Any]): String = set.toList match {
      case Nil => ""
      case x :: Nil => x.toString
      case _ => Tags.Keep
    }
    private def headOrKeepOrEmpty(set: Set[Any]): String = set.toList match {
      case Nil => Tags.ExplicitEmpty
      case x :: Nil => x.toString
      case _ => Tags.Keep
    }
    private def globalNamedTag(tagName: String, extractor: OptionalSong => Option[Any]): String =
      s"${tagName.toUpperCase}: ${headOrKeep(songs.flatMap(extractor(_)).toSet)}"
    private def requiredNamedTag(tagName: String, extractor: OptionalSong => Option[Any]): String =
      s"${tagName.toUpperCase}: ${headOrKeepOrEmpty(songs.flatMap(extractor(_)).toSet)}"
    def artist = requiredNamedTag(Tags.Artist, _.artistName)
    def album = requiredNamedTag(Tags.Album, _.albumName)
    def year = requiredNamedTag(Tags.Year, _.year)

    def composer = globalNamedTag(Tags.Composer, _.composer)
    def opus = globalNamedTag(Tags.Opus, _.opus)
    def conductor = globalNamedTag(Tags.Conductor, _.conductor)
    def orchestra = globalNamedTag(Tags.Orchestra, _.orchestra)
    def performanceYear = globalNamedTag(Tags.PerformanceYear, _.performanceYear)

    private def sequence[A](f: OptionalSong => String): Seq[String] = songs.map(f(_))
    def files: Seq[String] = sequence(_.file)
    def titles: Seq[String] = sequence(_.title.getOrElse(""))
    def tracks: Seq[Int] = songs.zipWithIndex
        .map(_.modifySecond(_ + 1))
        .map(_.reduce(_.track.getOrElse(_)))
    def discNumbers: Seq[String] = sequence(_.discNumber.getOrElse(""))
  }
  def apply(dir: DirectoryRef): InitialLines = {
    val $ = new Extractor(dir)
    val instructions = Vector(
      "# Lines starting with # will be ignored",
      "# All properties use the format 'TAG: value'; Empty and missing properties will be removed",
      s"# Use the special string '${Tags.Keep}' to avoid changing the ID3 tag",
      s"# Use the special string '${Tags.ExplicitEmpty}' for explicitly empty required tags, e.g., for year",
      "# Use ':cq' to exit with error, which stops the application and doesn't update any ID3 metadata.",
      "# ----------------------------",
    )
    val globalTags = Vector(
      "# Global ID3 tags; tags with more than single value are preset to " + Tags.Keep,
      "# First are the global and mandatory ID3 properties",
      $.artist,
      $.album,
      $.year,
      "# Classical tags",
      $.composer,
      $.opus,
      $.conductor,
      $.orchestra,
      $.performanceYear,
    ) ++ Flag.defaultInstructions ++ Vector(
      "# Individual tags",
      "# Individual tracks are pre-ordered by track number",
      "# FILE isn't actually a tag but is used later on in the process (so don't delete or modify it!)",
    )
    val individualTags: Seq[String] = $.files.zip($.titles).flatZip($.tracks).flatZip($.discNumbers)
        .map(Function.tupled(IndividualInitializer.IndividualTags)(_)) |> aux.apply

    val lines = instructions ++ globalTags ++ individualTags
    InitialLines(lines, startingEditLine = lines.indexWhere(_.startsWith("#").isFalse) + 1)
  }
}
private object Initializer {
  case class InitialLines(lines: Seq[String], startingEditLine: Int)
}
