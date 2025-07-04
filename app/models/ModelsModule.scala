package models

import net.codingwell.scalaguice.ScalaModule

object ModelsModule extends ScalaModule {
  override def configure(): Unit =
    bind[SongTagParser].toInstance(IOSongTagParser)
}
