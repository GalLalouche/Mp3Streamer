package backend.external

import backend.recon.{Album, Artist, ReconID}
import javax.inject.Inject

private class ArtistLinkExtractor @Inject()(helper: MbHtmlLinkExtractorHelper)
    extends ExternalLinkProvider[Artist] {
  private val aux = helper[Artist]("artist") _
  override def apply(v1: ReconID) = aux(v1)
}
private class AlbumLinkExtractor @Inject()(helper: MbHtmlLinkExtractorHelper)
    extends ExternalLinkProvider[Album] {
  private val aux = helper[Album]("release-group") _
  override def apply(v1: ReconID) = aux(v1)
}
