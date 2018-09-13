package backend.external.recons

import backend.recon.{Album, Artist}
import com.google.inject.Provides
import net.codingwell.scalaguice.ScalaModule

private[external] object ReconsModule extends ScalaModule {
  @Provides
  private def provideArtistLinkRetrievers(lastFmReconciler: LastFmLinkRetriever): LinkRetrievers[Artist] =
    new LinkRetrievers[Artist](Vector(lastFmReconciler))

  @Provides
  private def provideAlbumLinkRetrievers: LinkRetrievers[Album] = new LinkRetrievers[Album](Nil)
}
