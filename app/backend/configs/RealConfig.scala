package backend.configs

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.google.inject.Module
import play.api.libs.ws.ahc.StandaloneAhcWSClient
import slick.jdbc.{JdbcProfile, SQLiteProfile}

trait RealConfig extends Configuration {
  override lazy val profile: JdbcProfile = SQLiteProfile
  override lazy val db: profile.backend.DatabaseDef =
    profile.api.Database.forURL("jdbc:sqlite:d:/media/music/MBRecon.sqlite", driver = "org.sqlite.JDBC")
  private lazy val materializer = ActorMaterializer()(ActorSystem.create("RealConfigWS-System"))

  override def module: Module = RealModule
  override protected def createWsClient() = StandaloneAhcWSClient()(materializer)
}
