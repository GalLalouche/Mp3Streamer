package backend.configs

import java.time.Clock

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import common.io.{DirectoryRef, IODirectory}
import models.IOMusicFinder
import play.api.libs.ws.ahc.AhcWSClient
import slick.driver.{JdbcProfile, SQLiteDriver}

trait RealConfig extends Configuration {
  override lazy implicit val driver: JdbcProfile = SQLiteDriver
  override implicit lazy val db: driver.backend.DatabaseDef =
    driver.api.Database.forURL("jdbc:sqlite:d:/media/music/MBRecon.sqlite", driver = "org.sqlite.JDBC")
  override implicit lazy val mf: IOMusicFinder = IOMusicFinder
  override implicit lazy val rootDirectory: DirectoryRef = IODirectory.apply("D:/media/streamer/")
  override implicit val clock = Clock.systemDefaultZone
  override def ws = AhcWSClient()(ActorMaterializer()(ActorSystem.create("RealConfigWS-System")))
}
