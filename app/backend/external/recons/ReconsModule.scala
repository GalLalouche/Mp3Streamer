package backend.external.recons

import backend.recon.{Album, Artist}
import com.google.inject.Provides
import net.codingwell.scalaguice.ScalaModule

private[external] object ReconsModule extends ScalaModule {
  @Provides private def artistLinkRetrievers(
      lastFmReconciler: LastFmLinkRetriever,
  ): LinkRetrievers[Artist] = LinkRetrievers[Artist](lastFmReconciler)

  @Provides private def albumLinkRetrievers: LinkRetrievers[Album] = LinkRetrievers[Album]()
}
