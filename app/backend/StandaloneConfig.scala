package backend
import models.{MusicFinder, RealLocations}
import slick.driver.{JdbcProfile, SQLiteDriver}

import scala.concurrent.ExecutionContext

object StandaloneConfig extends Configuration {
  override implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  override implicit val driver: JdbcProfile = SQLiteDriver
  override implicit val db = driver.api.Database.forURL("jdbc:sqlite:d:/media/music/MBRecon.sqlite", driver = "org.sqlite.JDBC")
  override implicit val mf: MusicFinder = RealLocations
}
