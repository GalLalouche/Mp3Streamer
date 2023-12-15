package backend.lyrics

import scala.collection.immutable

import enumeratum.{Enum, EnumEntry}
sealed trait LyricsUrl extends EnumEntry
object LyricsUrl extends Enum[LyricsUrl] {
  case class Url(url: io.lemonlabs.uri.Url) extends LyricsUrl
  case object OldData extends LyricsUrl
  case object DefaultEmpty extends LyricsUrl
  case object ManualEmpty extends LyricsUrl
  override def values: immutable.IndexedSeq[LyricsUrl] = findValues
}
