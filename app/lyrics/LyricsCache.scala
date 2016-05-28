package lyrics

import common.storage.OnlineRetrieverCacher
import models.Song

import common.RichFuture._
import java.io.File

import scala.concurrent.ExecutionContext

class LyricsCache(implicit ec: ExecutionContext) extends OnlineRetrieverCacher[Song, Lyrics](
  new LyricsStorage(), new CompositeLyricsRetriever(new LyricsWikiaRetriever(), new DarkLyricsRetriever())) {
}

object LyricsCache {
  import scala.concurrent.ExecutionContext.Implicits.global
  def main(args: Array[String]) {
    println(new LyricsCache().get(Song(new File("""D:\Media\Music\Rock\Hard-Rock\Led Zeppelin\1971 Led Zeppelin IV\02 - Rock and Roll.mp3"""))).get)
  }
}
