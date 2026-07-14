package mains.cover.image

import mains.cover.image.scrappa.ScrappaModule
import net.codingwell.scalaguice.ScalaModule

private[cover] object ImageModule extends ScalaModule {
  override def configure(): Unit =
    install(ScrappaModule)
}
