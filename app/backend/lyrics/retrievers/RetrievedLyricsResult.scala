package backend.lyrics.retrievers

import backend.lyrics.Lyrics

private[lyrics] sealed trait RetrievedLyricsResult

private[lyrics] object RetrievedLyricsResult {
  case object NoLyrics extends RetrievedLyricsResult // No lyrics found in site
  case class Error(e: Throwable) extends RetrievedLyricsResult
  case class RetrievedLyrics(l: Lyrics) extends RetrievedLyricsResult
}
