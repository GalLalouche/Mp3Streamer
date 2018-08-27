package backend.configs

import java.time.Clock

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import backend.logging.Logger
import backend.storage.DbProvider
import com.google.inject.Provides
import common.io.{DirectoryRef, InternetTalker, IODirectory, RootDirectory}
import common.io.WSAliases.WSClient
import models.{IOMusicFinder, MusicFinder}
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.ws.ahc.StandaloneAhcWSClient
import slick.jdbc.{JdbcProfile, SQLiteProfile}

import scala.concurrent.ExecutionContext

object RealModule extends ScalaModule {
  override def configure(): Unit = {
    bind[Clock] toInstance Clock.systemDefaultZone
    bind[DirectoryRef].annotatedWith[RootDirectory] toInstance IODirectory.apply("D:/media/streamer/")
    bind[IOMusicFinder] toInstance IOMusicFinder
    bind[DbProvider] toInstance new DbProvider {
      override lazy val profile: JdbcProfile = SQLiteProfile
      override lazy val db: profile.backend.DatabaseDef =
        profile.api.Database.forURL("jdbc:sqlite:d:/media/music/MBRecon.sqlite", driver = "org.sqlite.JDBC")
    }
    bind[ActorMaterializer] toInstance ActorMaterializer()(ActorSystem.create("RealConfigWS-System"))

    requireBinding(classOf[Logger])
    requireBinding(classOf[ExecutionContext])
  }

  @Provides
  private def provideMusicFinder(mf: IOMusicFinder): MusicFinder = mf

  @Provides
  private def provideInternetTalker(
      _ec: ExecutionContext, materializer: ActorMaterializer): InternetTalker = new InternetTalker {
    override def execute(runnable: Runnable) = _ec.execute(runnable)
    override def reportFailure(cause: Throwable) = _ec.reportFailure(cause)
    override protected def createWsClient(): WSClient = StandaloneAhcWSClient()(materializer)
  }
}
