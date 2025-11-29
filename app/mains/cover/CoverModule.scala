package mains.cover

import net.codingwell.scalaguice.ScalaModule

import common.guice.ModuleUtils
import common.io.google.GoogleModule

private[mains] object CoverModule extends ScalaModule with ModuleUtils {
  override def configure(): Unit = {
    install(GoogleModule)
    install[AsyncFolderImagePanelFactory]
  }
}
