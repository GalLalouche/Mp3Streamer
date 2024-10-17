package backend.lyrics.retrievers

import models.Song

import scala.concurrent.{ExecutionContext, Future}

private[lyrics] class CompositeLyricsRetriever(
    retrievers: Seq[LyricsRetriever],
)(implicit ec: ExecutionContext)
    extends LyricsRetriever {
  override def get = (s: Song) =>
    retrievers.foldLeft(retrievers.head.apply(s))((result, nextRetriever) =>
      result.flatMap {
        case RetrievedLyricsResult.NoLyrics => nextRetriever(s)
        case RetrievedLyricsResult.Error(e) =>
          scribe.error("Failed to parse lyrics: ", e)
          nextRetriever(s)
        case e: RetrievedLyricsResult.RetrievedLyrics => Future.successful(e)
      },
    )
}
