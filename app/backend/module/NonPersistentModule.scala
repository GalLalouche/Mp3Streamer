package backend.module

import java.time.Clock

import backend.logging.{ConsoleLogger, FilteringLogger, Logger}
import backend.storage.DbProvider
import common.io.{DirectoryRef, MemoryRoot, RootDirectory}
import models.{IOMusicFinder, MusicFinder}
import net.codingwell.scalaguice.ScalaModule
import slick.jdbc.{H2Profile, JdbcProfile}

import scala.concurrent.ExecutionContext

object NonPersistentModule extends ScalaModule {
  override def configure(): Unit = {
    install(AllModules)

    bind[Clock] toInstance Clock.systemDefaultZone
    bind[Logger] toInstance new ConsoleLogger with FilteringLogger
    bind[DirectoryRef].annotatedWith[RootDirectory] toInstance new MemoryRoot
    bind[MusicFinder] toInstance new IOMusicFinder
    bind[DbProvider] toInstance new DbProvider {
      override lazy val profile: JdbcProfile = H2Profile
      override lazy val db: profile.backend.DatabaseDef =
        profile.api.Database.forURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
    }
    bind[ExecutionContext] toInstance ExecutionContext.Implicits.global

    install(RealInternetTalkerModule.daemonic)
    install(AllModules)
  }
}
