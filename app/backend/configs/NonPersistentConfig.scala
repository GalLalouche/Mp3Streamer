package backend.configs

import com.google.inject.Module
import slick.jdbc.{H2Profile, JdbcProfile}

trait NonPersistentConfig extends Configuration {
  override lazy val profile: JdbcProfile = H2Profile
  override lazy val db: profile.backend.DatabaseDef =
    profile.api.Database.forURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
  override def module: Module = NonPersistentModule
}
