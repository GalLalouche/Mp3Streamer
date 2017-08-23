package backend.configs

import java.net.HttpURLConnection
import java.time.Clock

import backend.Url
import common.io.{DirectoryRef, IODirectory}
import models.IOMusicFinder
import slick.driver.{JdbcProfile, SQLiteDriver}

trait RealConfig extends Configuration {
  override lazy implicit val driver: JdbcProfile = SQLiteDriver
  override implicit lazy val db: driver.backend.DatabaseDef = driver.api.Database.forURL("jdbc:sqlite:d:/media/music/MBRecon.sqlite", driver = "org.sqlite.JDBC")
  override implicit lazy val mf: IOMusicFinder = IOMusicFinder
  override protected def connection(u: Url) = u.toURL.openConnection().asInstanceOf[HttpURLConnection]
  override implicit lazy val rootDirectory: DirectoryRef = IODirectory.apply("D:/media/streamer/")
  override implicit val clock = Clock.systemDefaultZone
}
