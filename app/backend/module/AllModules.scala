package backend.module

import backend.albums.NewAlbumsModule
import backend.mb.MbModule
import backend.pkg.PkgModule
import backend.search.SearchModule
import decoders.EncoderModule
import net.codingwell.scalaguice.ScalaModule
import songs.SongsModule

object AllModules extends ScalaModule {
  override def configure(): Unit = {
    install(MbModule)
    install(SongsModule)
    install(SearchModule)
    install(PkgModule)
    install(NewAlbumsModule)
    install(EncoderModule)
  }
}
