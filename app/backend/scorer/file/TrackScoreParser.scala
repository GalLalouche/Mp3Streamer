package backend.scorer.file

import backend.recon.Track

import scala.util.{Failure, Try}

import common.rich.func.MoreTryInstances._
import scalaz.syntax.apply.ToApplyOps

import common.rich.primitives.RichString._

private object TrackScoreParser extends ScoreParserTemplate[Track] {
  protected override val prefix = "SONG"
  protected override def entity(s: Seq[String]): Try[Track] = s.toVector match {
    case Vector(_, _, song) =>
      (parseTitle(song) |@| AlbumScoreParser.entity(s.take(2)))(Track.apply)
    case _ => Failure(new Exception(s"Invalid entry: '$s'"))
  }

  private val TrackPattern = """\d+\. (.*)""".r
  private def parseTitle(str: String): Try[String] = Try(str.captureWith(TrackPattern))
}
