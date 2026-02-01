package musicfinder

import models.ModelJsonable
import net.codingwell.scalaguice.ScalaModule

import common.io._
import common.io.avro.ModelAvroable
import common.path.ref.{DirectoryRef, PathRefFactory}
import common.path.ref.io.{IODirectory, IOPathRefFactory}

object IOMusicFinderModule extends ScalaModule {
  private[musicfinder] val BaseDir: IODirectory = IODirectory("G:/media/music")
  override def configure(): Unit = {
    bind[DirectoryRef].annotatedWith[BaseDirectory].toInstance(BaseDir)
    bind[IODirectory].annotatedWith[BaseDirectory].toInstance(BaseDir)
    bind[MusicFiles].to[IOMusicFilesImpl]
    bind[IOMusicFiles].to[IOMusicFilesImpl]
    bind[SongFileFinder].to[IOSongFileFinder]
    bind[IOSongFileFinder].toInstance(new IOSongFileFinder)
    bind[ModelJsonable.SongParser].toInstance(ModelJsonable.IOSongJsonParser)
    bind[ModelAvroable.SongParser].toInstance(ModelAvroable.IOSongAvroParser)
    bind[PathRefFactory].toInstance(IOPathRefFactory)
    bind[PosterLookup].toInstance(PosterLookup.IOPosterLookup)
  }
}
