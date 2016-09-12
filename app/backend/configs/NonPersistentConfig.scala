package backend.configs

import common.io.{DirectoryRef, MemoryRoot}
import slick.driver.{H2Driver, JdbcProfile}


trait NonPersistentConfig extends Configuration {
  override lazy implicit val driver: JdbcProfile = H2Driver
  override implicit lazy val db: driver.backend.DatabaseDef =
    driver.api.Database.forURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.H2.JDBC")
  override implicit lazy val rootDirectory: DirectoryRef = new MemoryRoot
}
