package backend.scorer.file

import backend.recon.Artist

import scala.util.{Failure, Success}

private object ArtistScoreParser extends ScoreParserTemplate[Artist] {
  protected override val prefix = "ARTIST"
  protected override def entity(sections: Seq[String]) = sections.toVector match {
    case Vector(a) => Success(Artist(a))
    case _ => Failure(new Exception(s"Invalid entry: '$sections'"))
  }
}
