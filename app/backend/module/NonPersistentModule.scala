package backend.module

import java.time.Clock

import backend.storage.DbProvider
import net.codingwell.scalaguice.ScalaModule
import slick.jdbc.{H2Profile, JdbcProfile}

import common.io.{DirectoryRef, MemoryRoot, RootDirectory}

object NonPersistentModule extends ScalaModule {
  override def configure(): Unit = {
    install(StandaloneModule)

    bind[Clock] toInstance Clock.systemDefaultZone
    bind[DirectoryRef].annotatedWith[RootDirectory] toInstance new MemoryRoot
    bind[DbProvider] toInstance new DbProvider {
      override lazy val profile: JdbcProfile = H2Profile
      override lazy val db: profile.backend.DatabaseDef =
        profile.api.Database.forURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
    }
  }
}
