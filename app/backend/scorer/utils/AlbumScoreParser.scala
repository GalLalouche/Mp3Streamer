package backend.scorer.utils

import backend.recon.{Album, Artist}
 
import scala.util.{Failure, Success, Try}

import common.rich.primitives.RichString._

private object AlbumScoreParser extends ScoreParserTemplate[Album] {
  protected override val prefix = "ALBUM"
  protected override def entity(sections: Seq[String]): Try[Album] = sections.toVector match {
    case Vector(artist, titleYear) =>
      if (titleYear.matches(AlbumPattern)) {
        val year = titleYear.takeRight("2000)".length).dropRight(1).toInt
        val title = titleYear.dropRight(" (2000)".length)
        Success(Album(title, year, Artist(artist)))
      } else
        Failure(new Exception(s"Invalid album: '$titleYear'"))
    case _ => Failure(new Exception(s"Invalid entry: '$sections'"))
  }

  private val AlbumPattern = """.* \(\d{4}\)""".r.pattern
}
