package backend.configs

import java.time.Clock

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import common.io.{DirectoryRef, IODirectory}
import models.IOMusicFinder
import play.api.libs.ws.ahc.StandaloneAhcWSClient
import slick.jdbc.{JdbcProfile, SQLiteProfile}

trait RealConfig extends Configuration {
  override lazy implicit val profile: JdbcProfile = SQLiteProfile
  override implicit lazy val db: profile.backend.DatabaseDef =
    profile.api.Database.forURL("jdbc:sqlite:d:/media/music/MBRecon.sqlite", driver = "org.sqlite.JDBC")
  override implicit lazy val mf: IOMusicFinder = IOMusicFinder
  override implicit lazy val rootDirectory: DirectoryRef = IODirectory.apply("D:/media/streamer/")
  override implicit val clock = Clock.systemDefaultZone
  private lazy val materializer = ActorMaterializer()(ActorSystem.create("RealConfigWS-System"))
  override protected def createWsClient() = StandaloneAhcWSClient()(materializer)
}
