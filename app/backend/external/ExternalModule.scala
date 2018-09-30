package backend.external

import backend.external.expansions.ExpansionsModule
import backend.external.recons.ReconsModule
import backend.recon.{Album, Artist, ReconID}
import com.google.inject.Provides
import net.codingwell.scalaguice.ScalaModule

object ExternalModule extends ScalaModule {
  override def configure(): Unit = {
    install(ExpansionsModule)
    install(ReconsModule)
  }

  @Provides private def artistLinkExtractor(helper: MbHtmlLinkExtractorHelper): ExternalLinkProvider[Artist] =
    new ExternalLinkProvider[Artist] {
      val aux = helper[Artist]("artist") _
      override def apply(v1: ReconID) = aux(v1)
    }
  @Provides private def albumLinkExtractor(helper: MbHtmlLinkExtractorHelper): ExternalLinkProvider[Album] =
    new ExternalLinkProvider[Album] {
      val aux = helper[Album]("release-group") _
      override def apply(v1: ReconID) = aux(v1)
    }

  @Provides def artistReconStorage($: ArtistExternalStorage): ExternalStorage[Artist] = $
  @Provides def albumReconStorage($: AlbumExternalStorage): ExternalStorage[Album] = $
}
