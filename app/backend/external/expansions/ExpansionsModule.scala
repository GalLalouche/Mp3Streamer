package backend.external.expansions

import backend.recon.{Album, Artist}
import com.google.inject.Provides
import net.codingwell.scalaguice.ScalaModule

private[external] object ExpansionsModule extends ScalaModule {
  @Provides private def artistLinkExpanders(
      f: WikidataEnglishExtenderFactory,
  ): Traversable[ExternalLinkExpander[Artist]] = Vector(f.create)

  @Provides private def albumLinkExpanders(
      f: WikidataEnglishExtenderFactory,
      w: WikipediaAlbumExternalLinksExpander,
  ): Traversable[ExternalLinkExpander[Album]] = Vector(f.create, w)
}
