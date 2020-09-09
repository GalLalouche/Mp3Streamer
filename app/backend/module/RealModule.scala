package backend.module

import java.time.Clock

import backend.logging.Logger
import backend.storage.DbProvider
import com.google.inject.Provides
import javax.inject.Singleton
import models.IOMusicFinderModule
import net.codingwell.scalaguice.ScalaModule
import org.sqlite.SQLiteConfig
import slick.jdbc.{JdbcProfile, SQLiteProfile}

import scala.concurrent.ExecutionContext

import common.guice.ModuleUtils
import common.io.{DirectoryRef, InternetTalker, IODirectory, RootDirectory}
import common.rich.RichT._

object RealModule extends ScalaModule with ModuleUtils {
  override def configure(): Unit = {
    bind[Clock] toInstance Clock.systemDefaultZone
    bind[DbProvider] toInstance new DbProvider {
      private val props = new SQLiteConfig()
          .<|(_.enforceForeignKeys(true))
          .toProperties
      override lazy val profile: JdbcProfile = SQLiteProfile
      override lazy val db: profile.backend.DatabaseDef =
        profile.api.Database.forURL(
          "jdbc:sqlite:d:/media/streamer/data/MBRecon.sqlite",
          driver = "org.sqlite.JDBC",
          prop = props,
        )
    }

    requireBinding[Logger]
    requireBinding[ExecutionContext]
    requireBinding[InternetTalker]

    install(AllModules)
    install(IOMusicFinderModule)
  }

  @Provides @Singleton @RootDirectory private def rootDirectory: DirectoryRef =
    IODirectory.apply("D:/media/streamer/")
}
