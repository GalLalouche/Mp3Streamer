package musicfinder

import net.codingwell.scalaguice.ScalaModule

import common.io.{BaseDirectory, DirectoryRef, IODirectory}

object IOMusicFinderModule extends ScalaModule {
  private[musicfinder] val BaseDir: IODirectory = IODirectory("G:/media/music")
  override def configure(): Unit = {
    bind[DirectoryRef].annotatedWith[BaseDirectory].toInstance(BaseDir)
    bind[MusicFinder].to[IOMusicFinder]
    bind[PosterLookup].toInstance(PosterLookup.IOPosterLookup)
  }
}
