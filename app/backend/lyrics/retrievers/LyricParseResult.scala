package backend.lyrics.retrievers

private sealed trait LyricParseResult

private object LyricParseResult {
  case object NoLyrics extends LyricParseResult
  case object Instrumental extends LyricParseResult
  case class Lyrics(html: String) extends LyricParseResult
  case class Error(e: Exception) extends LyricParseResult
}
