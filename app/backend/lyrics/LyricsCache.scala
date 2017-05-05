package backend.lyrics

import java.io.File

import backend.{Retriever, Url}
import backend.configs.{Configuration, StandaloneConfig}
import backend.lyrics.retrievers._
import backend.storage.OnlineRetrieverCacher
import common.rich.RichFuture._
import models.Song

import scala.concurrent.Future

class LyricsCache(implicit c: Configuration) extends LyricsRetriever {
  private val retriever: LyricsRetriever =
    new CompositeLyricsRetriever(new LyricsWikiaRetriever(), new DarkLyricsRetriever(), new AzLyricsRetriever())
  private val cache = new OnlineRetrieverCacher[Song, Lyrics](
    new LyricsStorage(), new Retriever[Song, Lyrics] {override def apply(v1: Song): Future[Lyrics] = retriever.find(v1)})
  override def find(s: Song): Future[Lyrics] = cache(s)
  override def doesUrlMatchHost(url: Url): Boolean = retriever.doesUrlMatchHost(url)
  override def parse(url: Url, s: Song): Future[Lyrics] = {
    for (lyrics <- retriever.parse(url, s)) yield {
      cache.forceStore(s, lyrics)
      lyrics
    }

  }
}

object LyricsCache {
  private implicit val c = StandaloneConfig

  def main(args: Array[String]) {
    println(new LyricsCache().find(Song(new File("""D:\Media\Music\Rock\Hard-Rock\Led Zeppelin\1971 Led Zeppelin IV\02 - Rock and Roll.mp3"""))).get)
  }
}
