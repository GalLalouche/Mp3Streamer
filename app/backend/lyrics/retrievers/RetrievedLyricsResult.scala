package backend.lyrics.retrievers

import backend.lyrics.Lyrics
import backend.Url

private[lyrics] sealed trait RetrievedLyricsResult

private[lyrics] object RetrievedLyricsResult {
  case object NoLyrics extends RetrievedLyricsResult // No lyrics found in site
  case class Error(e: Throwable) extends RetrievedLyricsResult
  object Error {
    def unsupportedHost(url: Url): Error =
      Error(new NoSuchElementException(s"No retriever could parse host <${url.host}>"))
  }
  case class RetrievedLyrics(l: Lyrics) extends RetrievedLyricsResult
}
