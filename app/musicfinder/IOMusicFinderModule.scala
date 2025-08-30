package musicfinder

import models.ModelJsonable
import models.ModelJsonable.SongParser
import net.codingwell.scalaguice.ScalaModule

import common.io.{BaseDirectory, DirectoryRef, IODirectory, IOPathRefFactory, PathRefFactory}

object IOMusicFinderModule extends ScalaModule {
  private[musicfinder] val BaseDir: IODirectory = IODirectory("G:/media/music")
  override def configure(): Unit = {
    bind[DirectoryRef].annotatedWith[BaseDirectory].toInstance(BaseDir)
    bind[IODirectory].annotatedWith[BaseDirectory].toInstance(BaseDir)
    bind[MusicFinder].to[IOMusicFinder]
    bind[SongParser].toInstance(ModelJsonable.IOSongJsonParser)
    bind[PathRefFactory].toInstance(IOPathRefFactory)
    bind[PosterLookup].toInstance(PosterLookup.IOPosterLookup)
  }
}
