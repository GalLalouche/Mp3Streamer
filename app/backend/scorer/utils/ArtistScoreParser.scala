package backend.scorer.utils

import scala.util.Try

import backend.recon.Artist
import backend.scorer.ModelScore

private object ArtistScoreParser {
  private object Parser extends ParseUtils[(Artist, Option[ModelScore])] {
    private val prefix = """\**""".r
    private val header = "ARTIST ;"
    private val artistName = ".* ===".r.map(_.dropRight(4)) ^^ Artist.apply
    override val main = (prefix ~> header ~> artistName) ~ score ^^ { case name ~ score =>
      (name, score)
    }
  }
  def apply(line: String): Try[(Artist, Option[ModelScore])] = Parser.toTry(line)
}
