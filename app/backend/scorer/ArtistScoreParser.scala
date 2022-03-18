package backend.scorer

import backend.recon.Artist

import scala.util.Try

private object ArtistScoreParser {
  private object Parser extends ParseUtils[(Artist, ModelScore)] {
    private val prefix = """\**""".r
    private val header = "ARTIST ;"
    private val artistName = ".* ===".r.map(_.dropRight(4)) ^^ Artist.apply
    override val main = (prefix ~> header ~> artistName) ~ score ^^ {
      case name ~ score => (name, score)
    }
  }
  def apply(line: String): Try[(Artist, ModelScore)] = Parser.toTry(line)
}
