package backend.lyrics

import backend.Url
import backend.configs.{Configuration, StandaloneConfig}
import backend.lyrics.retrievers._
import backend.storage.OnlineRetrieverCacher
import common.rich.RichFuture._
import models.Song

import scala.concurrent.Future
import scalaz.std.FutureInstances
import scalaz.syntax.ToFunctorOps

class LyricsCache(implicit c: Configuration)
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
    new CompositeLyricsRetriever(firstDefaultRetrievers, htmlComposites, lastDefaultRetrievers)
  private val cache = new OnlineRetrieverCacher[Song, Lyrics](new LyricsStorage(), allComposite.find)
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
  private implicit val c = StandaloneConfig

  def main(args: Array[String]) {
    println(new InstrumentalArtistStorage().delete("satyricon").get)
  }
}
