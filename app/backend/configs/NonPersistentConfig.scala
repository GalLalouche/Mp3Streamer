package backend.configs

import backend.logging.{ConsoleLogger, FilteringLogger, Logger}
import common.io.{DirectoryRef, MemoryRoot}
import slick.jdbc.{H2Profile, JdbcProfile}

trait NonPersistentConfig extends Configuration {
  override lazy implicit val profile: JdbcProfile = H2Profile
  override implicit lazy val db: profile.backend.DatabaseDef =
    profile.api.Database.forURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
  override implicit lazy val rootDirectory: DirectoryRef = new MemoryRoot
  override implicit val logger: Logger = new ConsoleLogger with FilteringLogger
}
