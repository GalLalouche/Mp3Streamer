package lyrics

import common.storage.OnlineRetrieverCacher
import models.Song
import scala.concurrent.ExecutionContext.Implicits.global
import common.RichFuture._
import java.io.File

object LyricsCache extends OnlineRetrieverCacher[Song, Lyrics](LyricsStorage, LyricsWikiaRetriever) {
  def main(args: Array[String]) {
    println(get(Song(new File("""D:\Media\Music\Rock\Hard-Rock\Led Zeppelin\1971 Led Zeppelin IV\02 - Rock and Roll.mp3"""))).get)
  }
}
