package backend.lyrics.retrievers

import backend.Url
import backend.lyrics.{HtmlLyrics, Instrumental, Lyrics}
import common.io.InternetTalker
import models.Song
import org.jsoup.nodes.Document

import scala.concurrent.{ExecutionContext, Future}

private[lyrics] trait HtmlRetriever extends LyricsRetriever {
  def doesUrlMatchHost(url: Url): Boolean
  def parse(url: Url, s: Song): Future[Lyrics]
}
