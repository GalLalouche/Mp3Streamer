package backend.external.mark

import backend.recon.{Album, Artist}
import com.google.inject.Provides
import net.codingwell.scalaguice.ScalaModule

private[external] object MarkModule extends ScalaModule {
  @Provides private def albumLinkMarkers(
      wikidataNameMarkerFactory: WikidataNameMarkerFactory,
  ): Traversable[ExternalLinkMarker[Album]] =
    Vector(wikidataNameMarkerFactory.create)
  @Provides private def artistLinkMarkers(
      wikidataNameMarkerFactory: WikidataNameMarkerFactory,
  ): Traversable[ExternalLinkMarker[Artist]] =
    Vector(wikidataNameMarkerFactory.create)
}
