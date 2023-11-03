package backend.scorer.utils

import scala.util.Try

import backend.recon.{Album, Artist}
import backend.scorer.ModelScore

private object AlbumScoreParser {
  private object Parser extends ParseUtils[(Album, Option[ModelScore])] {
    private val prefix = """\**""".r
    private val header = "ALBUM ;"
    private val artist = ".* ;;;".r.map(_.dropRight(" ;;;".length)) ^^ Artist.apply
    private val titleYear =
      """.* \(\d{4}\)""".r ^^ { str =>
        val year = str.takeRight("2000)".length).dropRight(1).toInt
        val title = str.dropRight(" (2000)".length)
        (title, year)
      }
    override val main = (prefix ~> header ~> artist) ~ titleYear ~ ("===" ~> score) ^^ {
      case artistName ~ titleYear ~ score => Album(titleYear._1, titleYear._2, artistName) -> score
    }
  }
  def apply(line: String): Try[(Album, Option[ModelScore])] = Parser.toTry(line)
}
