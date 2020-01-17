package backend.lyrics.retrievers

import com.google.inject.Provides
import net.codingwell.scalaguice.ScalaModule

private[lyrics] object RetrieversModule extends ScalaModule {
  @Provides @CompositePassiveParser private def provideCPP(
      bandcampParser: BandcampParser
  ): PassiveParser = PassiveParser.composite(Vector(bandcampParser))
}
