package mains.vimtag

import javax.inject.Inject

import common.io.DirectoryRef
import common.rich.collections.RichSeq._
import common.rich.primitives.RichBoolean.richBoolean
import common.rich.RichT._
import common.rich.RichTuple.richTuple2
import mains.vimtag.Initializer.InitialLines
import models.{MusicFinder, OptionalSong}

/** Sets up the initial lines and writes usage instructions. */
private class Initializer @Inject() (mf: MusicFinder, aux: IndividualInitializer) {
  private class Extractor(dir: DirectoryRef) {
    private lazy val songFiles = (dir +: dir.dirs).flatMap(mf.getOptionalSongsInDir)
    private lazy val ordering: Ordering[OptionalSong] =
      if (songFiles.ensuring(_.nonEmpty).forall(_.track.isDefined))
        Ordering.by(_.toTuple(_.directory, _.track))
      else
        Ordering.by(_.file)

    private lazy val songs = songFiles.sorted(ordering).toVector
    private def globalNamedTag[A](tagName: String, extractor: OptionalSong => Option[A]) =
      OptionalField(tagName.toUpperCase, songs.flatMap(extractor(_)))
    private def requiredNamedTag[A](tagName: String, extractor: OptionalSong => Option[A]) =
      RequiredField(tagName.toUpperCase, songs.flatMap(extractor(_)))
    def artist = requiredNamedTag(Tags.Artist, _.artistName)
    def album = requiredNamedTag(Tags.Album, _.albumName)
    def year = requiredNamedTag(Tags.Year, _.year)

    def composer = globalNamedTag(Tags.Composer, _.composer)
    def opus = globalNamedTag(Tags.Opus, _.opus)
    def conductor = globalNamedTag(Tags.Conductor, _.conductor)
    def orchestra = globalNamedTag(Tags.Orchestra, _.orchestra)
    def performanceYear = globalNamedTag(Tags.PerformanceYear, _.performanceYear)

    private def sequence(f: OptionalSong => String): Seq[String] = songs.map(f(_))
    private def relativize(s: String): String = s
      .ensuring(_.startsWith(dir.path), s"Directory: <${dir.path}> is not a prefix to file <$s>")
      .drop(dir.path.length)
      .stripPrefix("/")
    def files: Seq[String] = sequence(_.file |> relativize)
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
      s"# Global ID3 tags; tags with more than single value are preset to ${Tags.Keep}. Special <TAGS> are case insensitive.",
      s"# ${Tags.Common} uses the most common value; counts appear above the ${Tags.Keep} as comments",
      "# First are the global and mandatory ID3 properties",
      $.artist.lines,
      $.album.lines,
      $.year.lines,
      s"# Classical tags. Ignored if ${$.performanceYear.key} is ${Tags.ExplicitEmpty}",
      $.composer.lines,
      $.opus.lines,
      $.conductor.lines,
      $.orchestra.lines,
      $.performanceYear.lines,
      Flag.defaultInstructions,
      "# Individual tags",
      "# Individual tracks are pre-ordered by track number",
      "# FILE isn't actually a tag but is used later on in the process (so don't delete or modify it!)",
    ) |> Initializer.flatten
    val individualTags: Seq[String] = $.files
      .zip($.titles)
      .flatZip($.tracks)
      .flatZip($.discNumbers)
      .map(Function.tupled(IndividualInitializer.IndividualTags)(_)) |> aux.apply

    val lines = instructions ++ globalTags ++ individualTags
    InitialLines(
      lines,
      startingEditLine = lines.indexWhere(_.startsWith("#").isFalse) + 1,
      initialValues = InitialValues.from(
        $.artist,
        $.album,
        $.year,
        $.composer,
        $.opus,
        $.conductor,
        $.orchestra,
        $.performanceYear,
      ),
    )
  }
}
private object Initializer {
  private def flatten(xs: Seq[Any]): Seq[String] = xs.flatMap {
    case xs: Seq[_] => xs.map(_.toString)
    case s: String => Vector(s)
  }

  case class InitialLines(
      lines: Seq[String],
      startingEditLine: Int,
      initialValues: Map[String, InitialValues],
  )
}
