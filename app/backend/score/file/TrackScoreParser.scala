package backend.score.file

import backend.recon.Track

import scala.util.{Failure, Try}

import cats.syntax.apply.catsSyntaxApplyOps

import common.rich.primitives.RichString._

private object TrackScoreParser extends ScoreParserTemplate[Track] {
  protected override val prefix = "SONG"
  protected override def entity(s: Vector[String]): Try[Track] = s match {
    case Vector(_, _, song) =>
      parseTitle(song).map2(AlbumScoreParser.entity(s.take(2)))(Track.apply)
    case _ => Failure(new Exception(s"Invalid entry: '$s'"))
  }

  private val TrackPattern = """\d+\. (.*)""".r
  private def parseTitle(str: String): Try[String] = Try(str.captureWith(TrackPattern))
}
