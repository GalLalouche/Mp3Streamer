package backend.lyrics.retrievers

import backend.lyrics.retrievers.bandcamp.{BandcampAlbumRetriever, BandcampParser}
import com.google.inject.Provides
import net.codingwell.scalaguice.ScalaModule

import scala.concurrent.ExecutionContext

private[lyrics] object RetrieversModule extends ScalaModule {
  override def configure(): Unit =
    bind[InstrumentalArtistStorage].to[SlickInstrumentalArtistStorage]
  @Provides @CompositePassiveParser private def provideCPP(
      bandcampParser: BandcampParser,
      shironetParser: ShironetParser,
      musixMatchParser: MusixMatchParser,
  ): PassiveParser = PassiveParser.composite(bandcampParser, shironetParser, musixMatchParser)
  @Provides @CompositeAlbumParser private def provideCAP(
      ec: ExecutionContext,
      bandcampParser: BandcampAlbumRetriever,
  ): HtmlRetriever = new CompositeHtmlRetriever(ec, Vector(bandcampParser))
}
