package backend.score.file

import backend.recon.{Album, Artist}

import scala.util.{Failure, Success, Try}

import common.rich.primitives.RichString._

private object AlbumScoreParser extends ScoreParserTemplate[Album] {
  protected override val prefix = "ALBUM"
  override def entity(sections: Vector[String]): Try[Album] = sections match {
    case Vector(artist, titleYear) =>
      if (titleYear.matches(YearPattern)) {
        val year = titleYear.takeRight("2000)".length).dropRight(1).toInt
        val title = titleYear.dropRight(" (2000)".length)
        Success(Album(title, year, Artist(artist)))
      } else
        Failure(new Exception(s"Invalid album: '$titleYear'"))
    case _ => Failure(new Exception(s"Invalid entry: '$sections'"))
  }

  private val YearPattern = """.* \(\d{4}\)""".r.pattern
}
