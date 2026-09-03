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
    ScribeUtils.noLogs()
    install(CleanModule.overrideWith(OverridingModule))
  }

  @Provides
  private def provideMemoryRefFactory(root: MemoryRoot): MemoryRefFactory =
    MemoryRefFactory(root)

  private object OverridingModule extends ScalaModule {
    override def configure(): Unit = {
      bind[String].annotatedWith[AccessToken].toInstance("test-token")
      bind[DbProvider].toInstance(H2MemProvider.nextNew())
      bind[ModelJsonable.SongParser].to[MemorySongJsonableParser]
      bind[ModelAvroable.SongParser].to[MemorySongAvroableParser]
      bind[PathRefFactory].to[MemoryRefFactory]
      bind[SongTagParser].to[FakeMusicFiles]
      bind[MusicFiles].to[FakeMusicFiles]
      bind[Clock].to[FakeClock]
      bind[DirectoryRef].annotatedWith[RootDirectory].to[MemoryRoot]
    }
    @Provides
    private def posterLookup(@RootDirectory rootDirectory: DirectoryRef): PosterLookup =
      s => rootDirectory.addFile(s.title + ".poster.jpg")
  }
}
