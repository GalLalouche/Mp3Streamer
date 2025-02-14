package formatter

import backend.logging.{ScribeConfigLoader, ScribeUtils}
import backend.module.{RealInternetTalkerModule, RealModule}
import backend.new_albums.filler.ExistingAlbumsModules
import com.google.common.annotations.VisibleForTesting
import com.google.inject.util.Modules
import net.codingwell.scalaguice.ScalaModule
import scribe.Level

import scala.concurrent.ExecutionContext

/**
 * Since formatters are almost the highest level module in this project (only below the actual
 * server), this module contains almost all the required functionality. It is basically missing
 * ExecutionContext, since some frameworks, e.g., Play, supply their own.
 */
class FormatterModule @VisibleForTesting() (level: Option[Level]) extends ScalaModule {
  def this() = this(FormatterModule.defaultLogLevel)
  override def configure(): Unit = {
    install(Modules.`override`(RealModule).`with`(ExistingAlbumsModules.lazyAlbums))
    install(RealInternetTalkerModule.nonDaemonic)
    setScribeLogging()
    requireBinding(classOf[ExecutionContext])
  }

  private def setScribeLogging(): Unit = {
    // TODO file logging
    val envLevel = Option(System.getenv("log_level")).map(_.toLowerCase)
    val levelToUse = envLevel.flatMap(ScribeUtils.parseLevel).orElse(this.level)
    levelToUse match {
      case Some(value) =>
        ScribeUtils.setRootLevel(value)
        ScribeConfigLoader.go()
      case None => ScribeUtils.noLogs()
    }
  }
}

private object FormatterModule {
  /** Should only be used for testing! Set `None` for no logs. */
  private[formatter] var defaultLogLevel: Option[Level] = Some(Level.Debug)
}
