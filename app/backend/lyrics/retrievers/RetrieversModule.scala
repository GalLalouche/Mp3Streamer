package backend.lyrics.retrievers

import backend.logging.Logger
import backend.lyrics.retrievers.bandcamp.{BandcampAlbumRetriever, BandcampParser}
import com.google.inject.Provides
import net.codingwell.scalaguice.ScalaModule

import scala.concurrent.ExecutionContext

private[lyrics] object RetrieversModule extends ScalaModule {
  @Provides @CompositePassiveParser private def provideCPP(
      bandcampParser: BandcampParser,
      shironetParser: ShironetParser,
  ): PassiveParser = PassiveParser.composite(bandcampParser, shironetParser)
  @Provides @CompositeAlbumParser private def provideCAP(
      ec: ExecutionContext,
      bandcampParser: BandcampAlbumRetriever,
      logger: Logger,
  ): HtmlRetriever = new CompositeHtmlRetriever(ec, logger, Vector(bandcampParser))
}
