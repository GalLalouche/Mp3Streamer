package backend.configs

import backend.logging.{ConsoleLogger, FilteringLogger, Logger}
import common.io.{MemoryRoot, MemoryRootProvider}
import slick.jdbc.{H2Profile, JdbcProfile}

trait NonPersistentConfig extends Configuration with MemoryRootProvider {
  override lazy val profile: JdbcProfile = H2Profile
  override lazy val db: profile.backend.DatabaseDef =
    profile.api.Database.forURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
  override lazy val rootDirectory: MemoryRoot = new MemoryRoot
  override val logger: Logger = new ConsoleLogger with FilteringLogger
}
