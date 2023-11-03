package backend.lyrics

import enumeratum.{Enum, EnumEntry}

import scala.collection.immutable
sealed trait LyricsUrl extends EnumEntry
object LyricsUrl extends Enum[LyricsUrl] {
  case class Url(url: io.lemonlabs.uri.Url) extends LyricsUrl
  @deprecated("Use Url case class")
  def oldUrl(url: backend.Url): Url = Url(url.toLemonLabs)
  case object OldData extends LyricsUrl
  case object DefaultEmpty extends LyricsUrl
  case object ManualEmpty extends LyricsUrl
  override def values: immutable.IndexedSeq[LyricsUrl] = findValues
}
