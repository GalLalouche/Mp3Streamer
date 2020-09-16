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
import slick.util.AsyncExecutor

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
          .<|(_.setBusyTimeout(10000))
          .toProperties
          .<|(_.put("maxConnection", 4.asInstanceOf[AnyRef]))

      override lazy val profile: JdbcProfile = SQLiteProfile
      override lazy val db: profile.backend.DatabaseDef = profile.api.Database.forURL(
        url = "jdbc:sqlite:d:/media/streamer/data/MBRecon.sqlite",
        prop = props,
        driver = "org.sqlite.JDBC",
        // Scumbag Slick. Sets the defautl to something that it can later warn about. You might expect for
        // default() and default(defaultValuesUsedInTheGoddamnDefault) to be the same, but you'd be wrong.
        executor = AsyncExecutor.default("Slick SQLite", 20),
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
