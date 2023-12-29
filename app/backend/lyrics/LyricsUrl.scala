package backend.lyrics

import enumeratum.{Enum, EnumEntry}

import scala.collection.immutable

private sealed trait LyricsUrl extends EnumEntry
private object LyricsUrl extends Enum[LyricsUrl] {
  case class Url(url: io.lemonlabs.uri.Url) extends LyricsUrl
  case object OldData extends LyricsUrl
  case object DefaultEmpty extends LyricsUrl
  case object ManualEmpty extends LyricsUrl
  override def values: immutable.IndexedSeq[LyricsUrl] = findValues
}
