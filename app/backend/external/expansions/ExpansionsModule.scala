package backend.external.expansions

import backend.recon.{Album, Artist}
import com.google.inject.Provides
import net.codingwell.scalaguice.ScalaModule

private[external] object ExpansionsModule extends ScalaModule {
  @Provides private def artistLinkExpanders(
      w2w: WikipediaToWikidataExtenderFactory,
      f: WikidataEnglishOrHebrewExtenderFactory,
  ): Traversable[ExternalLinkExpander[Artist]] = Vector(f.create, w2w.create)

  @Provides private def albumLinkExpanders(
      f: WikidataEnglishOrHebrewExtenderFactory,
      w: WikipediaAlbumExternalLinksExpander,
      w2w: WikipediaToWikidataExtenderFactory,
  ): Traversable[ExternalLinkExpander[Album]] = Vector(f.create, w2w.create, w)
}
