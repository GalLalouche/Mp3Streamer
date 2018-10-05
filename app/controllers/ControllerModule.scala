package controllers

import net.codingwell.scalaguice.ScalaModule

object ControllerModule extends ScalaModule {
  override def configure(): Unit = {
    bind[UrlPathUtils] toInstance PlayUrlPathUtils
  }
}
