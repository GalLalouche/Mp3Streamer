package backend.configs

import java.time.Clock

import backend.logging.Logger
import backend.storage.DbProvider
import com.google.inject.Provides
import common.io.{DirectoryRef, IODirectory, RootDirectory}
import models.{IOMusicFinder, MusicFinder}
import net.codingwell.scalaguice.ScalaModule
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

    requireBinding(classOf[Logger])
    requireBinding(classOf[ExecutionContext])
  }

  @Provides
  private def provideMusicFinder(mf: IOMusicFinder): MusicFinder = mf
}
