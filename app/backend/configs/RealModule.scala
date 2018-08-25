package backend.configs

import java.time.Clock

import backend.logging.Logger
import com.google.inject.Provides
import common.io.{DirectoryRef, IODirectory, RootDirectory}
import models.{IOMusicFinder, MusicFinder}
import net.codingwell.scalaguice.ScalaModule

object RealModule extends ScalaModule {
  override def configure(): Unit = {
    bind[Clock] toInstance Clock.systemDefaultZone
    bind[DirectoryRef].annotatedWith[RootDirectory] toInstance IODirectory.apply("D:/media/streamer/")
    bind[IOMusicFinder] toInstance IOMusicFinder

    requireBinding(classOf[Logger])
  }

  @Provides
  private def provideMusicFinder(mf: IOMusicFinder): MusicFinder = mf
}
