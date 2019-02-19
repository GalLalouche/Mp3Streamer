package mains.vimtag

import common.io.DirectoryRef
import common.rich.collections.RichSeq._
import common.rich.RichTuple._
import common.rich.func.TuplePLenses
import javax.inject.Inject
import models.{IOMusicFinder, OptionalSong, SongTagParser}

/** Sets up the initial lines. */
private class Initializer @Inject()(mf: IOMusicFinder) {
  private class Extractor(dir: DirectoryRef) {
    private lazy val songs = (dir +: dir.deepDirs)
        .flatMap(mf.getSongFilesInDir)
        .map(SongTagParser optionalSong _.file)
        .sortBy(_.file)
    private def headOr(set: Set[Any]) = set.toList match {
      case Nil => ""
      case x :: Nil => x.toString
      case _ => Tags.Keep
    }
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

    private def sequence[A](tag: String, f: OptionalSong => A): Seq[String] = songs.map(s"$tag: " + f(_))
    def files: Seq[String] = sequence(Tags.File, _.file)
    def titles: Seq[String] = sequence(Tags.Title, _.title.getOrElse(""))
    def tracks: Seq[String] = songs.zipWithIndex
        .map(TuplePLenses.tuple2Second.modify(_ + 1))
        .map(_.reduce(_.track.getOrElse(_))).map(s"${Tags.Track}: ".+)
    def discNumbers: Seq[String] = sequence(Tags.DiscNo, _.discNumber.getOrElse(""))
  }
  def apply(dir: DirectoryRef): Seq[String] = {
    val $ = new Extractor(dir)
    val instructions = Vector(
      "# Lines starting with # will be ignored",
      "# All properties use the format 'TAG: value'; Empty and missing properties will be removed",
      s"# Use the special string '${Tags.Keep}' to avoid changing the ID3 tag",
      s"# Use the special string '${Tags.ExplicitEmpty}' for explicitly empty required tags, e.g., for year",
      "# Use ':cq' to exit with error, which stops application and doesn't update any ID3 metadata.",
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
      "# Individual tags",
      "# Individual tracks are pre-ordered by track number",
      "# FILE isn't actually a tag but is used later on in the process (so don't delete or modify it!)",
    )
    val individualTags = $.files.zip($.titles).flatZip($.tracks).flatZip($.discNumbers)
        .flatMap(_.productIterator.map(_.toString))

    instructions ++ globalTags ++ individualTags
  }
}
