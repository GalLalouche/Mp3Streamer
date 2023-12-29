package backend.recon

import java.util.regex.Pattern

import common.rich.RichT._
import common.rich.primitives.RichOption._
import common.rich.primitives.RichString._

case class ReconID(id: String) extends AnyVal
object ReconID {
  private val Hex = "[a-f0-9]"
  private val Prefix = Pattern.compile(s"^https?://musicbrainz.org/\\w+/")
  private val Regex = Pattern.compile(s"$Hex{8}-$Hex{4}-$Hex{4}-$Hex{4}-$Hex{12}")
  def validate(id: String): Option[ReconID] =
    ReconID(id.removeAll(Prefix)).optFilter(_.id.matches(Regex))
  def validateOrThrow(id: String): ReconID =
    validate(id).getOrThrow(new IllegalArgumentException(s"Invalid MusicBrainz id <$id>"))
}
