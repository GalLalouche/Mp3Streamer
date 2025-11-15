package backend.lyrics.retrievers

import backend.lyrics.retrievers.bandcamp.{BandcampAlbumRetriever, BandcampParser}
import backend.lyrics.retrievers.genius.GeniusLyricsRetriever
import com.google.inject.{Exposed, Provides}
import net.codingwell.scalaguice.ScalaPrivateModule

import scala.concurrent.ExecutionContext

import common.guice.PrivateModuleUtils

private[lyrics] object RetrieversModule extends ScalaPrivateModule with PrivateModuleUtils {
  override def configure(): Unit = {
    expose[InstrumentalArtistStorage]
    publicBind[InstrumentalArtistStorage].to[SlickInstrumentalArtistStorage]
  }

  @Provides private def provideBaseList(
      geniusLyricsRetriever: GeniusLyricsRetriever,
      azLyricsRetriever: AzLyricsRetriever,
      darkLyricsRetriever: DarkLyricsRetriever,
      bandcampParser: BandcampAlbumRetriever,
  ): Seq[AmphiRetriever] = Vector(
    geniusLyricsRetriever,
    // azLyricsRetriever, captcha blocked
    darkLyricsRetriever,
    bandcampParser,
  )
  @Provides @Exposed private def provideActive(
      ec: ExecutionContext,
      base: Seq[AmphiRetriever],
      defaultArtistInstrumental: InstrumentalArtist,
  ): ActiveRetriever = new CompositeActiveRetriever(
    DefaultClassicalInstrumental +: base :+ defaultArtistInstrumental,
  )(ec)

  @Provides @Exposed private def providePassive(
      ec: ExecutionContext,
      base: Seq[AmphiRetriever],
      bandcampParser: BandcampParser,
      shironetParser: ShironetParser,
      musixMatchParser: MusixMatchParser,
  ): PassiveParser =
    new CompositePassiveParser(base ++ Vector(shironetParser, musixMatchParser, bandcampParser))(ec)

}
