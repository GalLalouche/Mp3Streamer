package backend.lyrics

import backend.Url
import backend.logging.Logger
import backend.lyrics.retrievers._
import backend.storage.OnlineRetrieverCacher
import common.rich.RichFuture._
import javax.inject.Inject
import models.Song

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.FutureInstances
import scalaz.syntax.ToFunctorOps

private class LyricsCache @Inject()(
    ec: ExecutionContext,
    logger: Logger,
    defaultArtistInstrumental: InstrumentalArtist,
    htmlComposites: CompositeHtmlRetriever,
    lyricsStorage: LyricsStorage,
) extends FutureInstances with ToFunctorOps {
  private implicit val iec: ExecutionContext = ec
  private val firstDefaultRetrievers = DefaultClassicalInstrumental
  private val lastDefaultRetrievers = defaultArtistInstrumental
  private val allComposite = new CompositeLyricsRetriever(
    ec, logger, Vector(htmlComposites, lastDefaultRetrievers, firstDefaultRetrievers))
  private val cache = new OnlineRetrieverCacher[Song, Lyrics](lyricsStorage, allComposite)
  def find(s: Song): Future[Lyrics] = cache(s)
  def parse(url: Url, s: Song): Future[Lyrics] = htmlComposites.parse(url, s)
      .map(lyrics => {
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

  import backend.configs.{CleanConfiguration, Configuration}
  import net.codingwell.scalaguice.InjectorExtensions._

  private implicit val c: Configuration = CleanConfiguration
  private implicit val ec: ExecutionContext = c.injector.instance[ExecutionContext]

  def main(args: Array[String]): Unit = {
    val s = Song(new File("""D:\Media\Music\Rock\Punk\Heartsounds\2013 Internal Eyes\01 - A Total Separation of Self.mp3"""))
    val $ = c.injector.instance[LyricsCache]
    println($.find(s).get)
  }
}
