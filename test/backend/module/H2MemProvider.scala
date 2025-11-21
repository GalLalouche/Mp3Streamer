package backend.module

import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

import backend.storage.DbProvider
import slick.jdbc.{H2Profile, JdbcProfile}
import slick.util.AsyncExecutor

/** Creates a new in memory H2 database for each call to make(). */
private object H2MemProvider {
  def nextNew(): DbProvider = new DbProvider {
    override lazy val profile: JdbcProfile = H2Profile
    override lazy val db: profile.backend.DatabaseDef = {
      val dbId = s"${urlIndex.getAndIncrement()}"
      profile.api.Database.forURL(
        url = s"jdbc:h2:mem:test$dbId;DB_CLOSE_DELAY=-1",
        executor = AsyncExecutor.default(s"Testing DB <$dbId>", 20),
      )
    }
    override def constraintMangler(name: String) = s"${UUID.randomUUID()}_$name"
  }

  private val urlIndex = new AtomicInteger(0)
}
