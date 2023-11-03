package songs

import net.codingwell.scalaguice.ScalaModule
import songs.selector.SelectorModule

object SongsModule extends ScalaModule {
  override def configure(): Unit =
    install(SelectorModule)
}
