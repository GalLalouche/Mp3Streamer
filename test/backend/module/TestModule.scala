package backend.module

import java.time.Clock
import java.util.logging.LogManager
import scala.collection.mutable

import backend.logging.{Logger, StringBuilderLogger}
import com.google.inject.Provides
import common.guice.ModuleUtils
import common.io.{DirectoryRef, MemoryRoot, RootDirectory}
import common.FakeClock
import models.MusicFinder
import net.codingwell.scalaguice.ScalaModule

class TestModule extends ScalaModule with ModuleUtils {
  LogManager.getLogManager.readConfiguration(getClass.getResourceAsStream("/logging.properties"))
  override def configure() = {
    bind[FakeClock].toInstance(new FakeClock)
    bind[Logger].toInstance(new StringBuilderLogger(new mutable.StringBuilder))

    requireBinding[MemoryRoot]
    requireBinding[FakeMusicFinder]

    install(AllModules)
  }

  // Ensures that the non-fake bindings will be the same as the fake ones.
  @Provides
  private def provideClock(clock: FakeClock): Clock = clock

  @Provides
  @RootDirectory
  private def provideMemoryRoot(@RootDirectory directoryRef: MemoryRoot): DirectoryRef =
    directoryRef

  @Provides
  private def provideMusicFinder(mf: FakeMusicFinder): MusicFinder = mf
}
