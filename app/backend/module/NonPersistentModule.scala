package backend.module

import java.time.Clock
import java.util.UUID

import backend.logging.LoggingModules
import backend.storage.DbProvider
import models.IOMusicFinderModule
import net.codingwell.scalaguice.ScalaModule
import slick.jdbc.{H2Profile, JdbcProfile}

import scala.concurrent.ExecutionContext

import common.io.{DirectoryRef, MemoryRoot, RootDirectory}

object NonPersistentModule extends ScalaModule {
  override def configure(): Unit = {
    bind[Clock].toInstance(Clock.systemDefaultZone)
    bind[DirectoryRef].annotatedWith[RootDirectory].toInstance(new MemoryRoot)
    bind[DbProvider].toInstance(new DbProvider {
      override lazy val profile: JdbcProfile = H2Profile
      override lazy val db: profile.backend.DatabaseDef =
        profile.api.Database.forURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
      override def constraintMangler(name: String) = s"${UUID.randomUUID()}_$name"
    })
    bind[ExecutionContext].toInstance(ExecutionContext.Implicits.global)

    install(RealInternetTalkerModule.daemonic)
    install(AllModules)
    install(LoggingModules.ConsoleWithFiltering)
    install(IOMusicFinderModule)
  }
}
