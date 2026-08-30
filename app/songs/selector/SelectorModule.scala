package songs.selector

import com.google.inject.{Provides, Singleton}
import musicfinder.MusicFiles
import net.codingwell.scalaguice.ScalaModule
import songs.selector.filter.FilterModule

import scala.concurrent.ExecutionContext

import common.guice.ModuleUtils

private[songs] object SelectorModule extends ScalaModule with ModuleUtils {
  override def configure(): Unit = {
    requireBinding[ExecutionContext]
    requireBinding[MusicFiles]
    bind[SongSelector].to[SongSelectorState]
    install(FilterModule)
  }

  @Singleton @Provides private def provideConfigurableSongSelector(
      mssf: MultiStageSongSelectorFactory,
  ): ConfigurableSongSelector = mssf()
}
