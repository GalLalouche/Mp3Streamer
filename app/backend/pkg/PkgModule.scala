package backend.pkg

import common.ModuleUtils
import net.codingwell.scalaguice.ScalaModule

object PkgModule extends ScalaModule with ModuleUtils {
  override def configure(): Unit = {
    install[ZipperFactory]
  }
}
