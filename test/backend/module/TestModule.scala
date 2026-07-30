package backend.module

import java.time.Clock
import java.util.logging.LogManager

import backend.logging.ScribeUtils
import backend.lyrics.retrievers.genius.AccessToken
import backend.storage.DbProvider
import com.google.inject.Provides
import models.{ModelJsonable, SongTagParser}
import musicfinder.{FakeMusicFiles, MusicFiles, PosterLookup}
import net.codingwell.scalaguice.ScalaModule

import common.FakeClock
import common.guice.ModuleUtils
import common.guice.RichModule.richModule
import common.io.RootDirectory
import common.io.avro.ModelAvroable
import common.path.ref.{DirectoryRef, PathRefFactory}
import common.test.memory_ref.{MemoryRefFactory, MemoryRoot}

private object TestModule extends ScalaModule with ModuleUtils {
  LogManager.getLogManager.readConfiguration(getClass.getResourceAsStream("/logging.properties"))
  override def configure(): Unit = {
    bind[FakeClock].toInstance(new FakeClock)
    bind[DbProvider].toInstance(H2MemProvider.nextNew())
    bind[ModelJsonable.SongParser].to[MemorySongJsonableParser]
    bind[ModelAvroable.SongParser].to[MemorySongAvroableParser]
    bind[PathRefFactory].to[MemoryRefFactory]
    bind[SongTagParser].to[FakeMusicFiles]
    requireBinding[MemoryRoot]
    requireBinding[FakeMusicFiles]
    // TODO this should be a handler!
    // bind[Logger].toInstance(new StringBuilderLogger(new mutable.StringBuilder))
    ScribeUtils.noLogs()

    // TODO TestModule should probably depend on CleanModule directly,
    // since it's essentially a clean run on its own.
    install(StorageAutoCreateModule)
    install(AllModules.overrideWith(TestModule.posterLookup))
  }

  private val posterLookup = new ScalaModule {
    override def configure(): Unit =
      bind[String].annotatedWith[AccessToken].toInstance("test-token")
    @Provides private def posterLookup(@RootDirectory rootDirectory: DirectoryRef): PosterLookup =
      s => rootDirectory.addFile(s.title + ".poster.jpg")
  }

  @Provides
  private def provideMemoryRefFactory(root: MemoryRoot): MemoryRefFactory = MemoryRefFactory(root)

  // Ensures that the non-fake bindings will be the same as the fake ones.
  @Provides
  private def provideClock(clock: FakeClock): Clock = clock

  @Provides
  @RootDirectory
  private def provideMemoryRoot(directoryRef: MemoryRoot): DirectoryRef = directoryRef

  @Provides
  private def provideMusicFinder(mf: FakeMusicFiles): MusicFiles = mf
}
