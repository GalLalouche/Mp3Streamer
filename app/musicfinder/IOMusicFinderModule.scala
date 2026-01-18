package musicfinder

import models.ModelJsonable
import models.ModelJsonable.SongParser
import net.codingwell.scalaguice.ScalaModule

import common.io._

object IOMusicFinderModule extends ScalaModule {
  private[musicfinder] val BaseDir: IODirectory = IODirectory("G:/media/music")
  override def configure(): Unit = {
    bind[DirectoryRef].annotatedWith[BaseDirectory].toInstance(BaseDir)
    bind[IODirectory].annotatedWith[BaseDirectory].toInstance(BaseDir)
    bind[MusicFiles].to[IOMusicFilesImpl]
    bind[SongFileFinder].to[IOSongFileFinder]
    bind[IOSongFileFinder].toInstance(new IOSongFileFinder)
    bind[SongParser].toInstance(ModelJsonable.IOSongJsonParser)
    bind[PathRefFactory].toInstance(IOPathRefFactory)
    bind[PosterLookup].toInstance(PosterLookup.IOPosterLookup)
  }
}
