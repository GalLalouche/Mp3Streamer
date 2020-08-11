package backend.recon

import java.util.regex.Pattern

import common.rich.primitives.RichOption._
import common.rich.primitives.RichString._
import common.rich.RichT._

case class ReconID(id: String) extends AnyVal
object ReconID {
  private val Hex = "[a-f0-9]"
  private val Regex = Pattern compile s"$Hex{8}-$Hex{4}-$Hex{4}-$Hex{4}-$Hex{12}"
  def validate(id: String): Option[ReconID] = ReconID(id).optFilter(_.id matches Regex)
  def validateOrThrow(id: String): ReconID =
    validate(id) getOrThrow new IllegalArgumentException(s"Invalid MusicBrainz id <$id>")
}
