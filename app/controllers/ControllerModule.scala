package controllers

import com.google.inject.Provides
import net.codingwell.scalaguice.ScalaModule

object ControllerModule extends ScalaModule {
  override def configure(): Unit =
    bind[UrlPathUtils].toInstance(PlayUrlPathUtils)

  @Provides private def provideUrlDecoder(u: UrlPathUtils): UrlDecodeUtils = u
}
