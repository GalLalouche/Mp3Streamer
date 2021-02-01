package mains.cover

import mains.cover.image.ImageModule
import net.codingwell.scalaguice.ScalaModule

private[mains] object CoverModule extends ScalaModule {
  override def configure(): Unit = {
    install(ImageModule)
  }
}
