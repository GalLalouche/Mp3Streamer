package backend.configs

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.google.inject.Module
import common.io.{DirectoryRef, IODirectory}
import models.{IOMusicFinder, IOMusicFinderProvider}
import play.api.libs.ws.ahc.StandaloneAhcWSClient
import slick.jdbc.{JdbcProfile, SQLiteProfile}

trait RealConfig extends Configuration with IOMusicFinderProvider {
  override lazy val profile: JdbcProfile = SQLiteProfile
  override lazy val db: profile.backend.DatabaseDef =
    profile.api.Database.forURL("jdbc:sqlite:d:/media/music/MBRecon.sqlite", driver = "org.sqlite.JDBC")
  override lazy val mf: IOMusicFinder = IOMusicFinder
  override lazy val rootDirectory: DirectoryRef = IODirectory.apply("D:/media/streamer/")
  private lazy val materializer = ActorMaterializer()(ActorSystem.create("RealConfigWS-System"))

  override def module: Module = RealModule
  override protected def createWsClient() = StandaloneAhcWSClient()(materializer)
}
