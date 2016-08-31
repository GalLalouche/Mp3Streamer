package backend.lyrics

import java.io.File

import backend.Retriever
import backend.configs.{Configuration, StandaloneConfig}
import backend.storage.OnlineRetrieverCacher
import common.rich.RichFuture._
import models.Song

import scala.concurrent.Future

class LyricsCache(implicit c: Configuration) extends Retriever[Song, Lyrics] {
  private val r = new OnlineRetrieverCacher[Song, Lyrics](
    new LyricsStorage(), new CompositeLyricsRetriever(new LyricsWikiaRetriever(), new DarkLyricsRetriever()))
  override def apply(v1: Song): Future[Lyrics] = r(v1)
}

object LyricsCache {
  private implicit val c = StandaloneConfig

  def main(args: Array[String]) {
    println(new LyricsCache().apply(Song(new File( """D:\Media\Music\Rock\Hard-Rock\Led Zeppelin\1971 Led Zeppelin IV\02 - Rock and Roll.mp3"""))).get)
  }
}
