package backend.module

import java.time.Clock

import backend.logging.Logger
import backend.storage.DbProvider
import com.google.inject.Provides
import javax.inject.Singleton
import models.IOMusicFinderModule
import net.codingwell.scalaguice.ScalaModule
import slick.jdbc.{JdbcProfile, SQLiteProfile}

import scala.concurrent.ExecutionContext

import common.ModuleUtils
import common.io.{DirectoryRef, InternetTalker, IODirectory, RootDirectory}

object RealModule extends ScalaModule with ModuleUtils {
  override def configure(): Unit = {
    install(AllModules)

    bind[Clock] toInstance Clock.systemDefaultZone
    bind[DbProvider] toInstance new DbProvider {
      override lazy val profile: JdbcProfile = SQLiteProfile
      override lazy val db: profile.backend.DatabaseDef =
        profile.api.Database.forURL("jdbc:sqlite:d:/media/streamer/data/MBRecon.sqlite", driver = "org.sqlite.JDBC")
    }

    requireBinding[Logger]
    requireBinding[ExecutionContext]
    requireBinding[InternetTalker]

    // TODO why do I need two installs?
    install(AllModules)
    install(IOMusicFinderModule)
  }

  @Provides @Singleton @RootDirectory private def rootDirectory: DirectoryRef =
    IODirectory.apply("D:/media/streamer/")
}
