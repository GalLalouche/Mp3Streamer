package backend.lyrics

import backend.Url
import backend.configs.{CleanConfiguration, Configuration}
import backend.lyrics.retrievers._
import backend.storage.OnlineRetrieverCacher
import common.rich.RichFuture._
import models.Song

import scala.concurrent.Future
import scalaz.std.FutureInstances
import scalaz.syntax.ToFunctorOps

private class LyricsCache(implicit c: Configuration)
    extends FutureInstances with ToFunctorOps {
  private val defaultArtistInstrumental = new InstrumentalArtist
  private val firstDefaultRetrievers = DefaultClassicalInstrumental
  private val htmlComposites: CompositeHtmlRetriever = new CompositeHtmlRetriever(
    new LyricsWikiaRetriever(),
    new DarkLyricsRetriever(),
    new AzLyricsRetriever()
  )
  private val lastDefaultRetrievers = defaultArtistInstrumental
  val allComposite =
    new CompositeLyricsRetriever(htmlComposites, lastDefaultRetrievers, firstDefaultRetrievers)
  private val cache = new OnlineRetrieverCacher[Song, Lyrics](new LyricsStorage(), allComposite)
  def find(s: Song): Future[Lyrics] = cache(s)
  def parse(url: Url, s: Song): Future[Lyrics] =
    htmlComposites.parse(url, s).map(lyrics => {
      cache.forceStore(s, lyrics)
      lyrics
    })
  def setInstrumentalSong(s: Song): Future[Instrumental] = {
    val instrumental = Instrumental("Manual override")
    cache.forceStore(s, instrumental).>|(instrumental)
  }
  def setInstrumentalArtist(s: Song): Future[Instrumental] = defaultArtistInstrumental add s
}

object LyricsCache {
  import java.io.File

  private implicit val c: Configuration = CleanConfiguration

  def main(args: Array[String]): Unit = {
    val s = Song(new File("""D:\Media\Music\Rock\Punk\Heartsounds\2013 Internal Eyes\01 - A Total Separation of Self.mp3"""))
    val cache = new LyricsCache()
    println(cache.find(s).get)
  }
}
