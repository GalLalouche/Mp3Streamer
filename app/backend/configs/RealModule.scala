package backend.configs

import java.time.Clock

import backend.logging.Logger
import common.io.{DirectoryRef, IODirectory, RootDirectory}
import net.codingwell.scalaguice.ScalaModule

object RealModule extends ScalaModule {
  override def configure(): Unit = {
    bind[Clock] toInstance Clock.systemDefaultZone
    bind[DirectoryRef].annotatedWith[RootDirectory] toInstance IODirectory.apply("D:/media/streamer/")

    requireBinding(classOf[Logger])
  }
}
