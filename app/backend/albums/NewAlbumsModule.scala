package backend.albums

import common.ModuleUtils
import net.codingwell.scalaguice.ScalaModule

object NewAlbumsModule extends ScalaModule with ModuleUtils {
  override def configure(): Unit = {
    install[NewAlbumsRetrieverFactory]
  }
}
