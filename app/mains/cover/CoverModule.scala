package mains.cover

import mains.cover.image.ImageModule
import net.codingwell.scalaguice.ScalaModule

import common.guice.ModuleUtils

private[mains] object CoverModule extends ScalaModule with ModuleUtils {
  override def configure(): Unit = {
    install(ImageModule)
    install[AsyncFolderImagePanelFactory]
  }
}
