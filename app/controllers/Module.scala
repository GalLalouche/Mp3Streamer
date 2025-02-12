package controllers

import backend.logging.{ScribeConfigLoader, ScribeUtils}
import backend.module.{RealInternetTalkerModule, RealModule}
import backend.new_albums.filler.ExistingAlbumsModules
import com.google.common.annotations.VisibleForTesting
import com.google.inject.util.Modules
import net.codingwell.scalaguice.ScalaModule
import scribe.Level

// The primary constructor should only be used by tests.
class Module @VisibleForTesting() (level: Option[Level]) extends ScalaModule {
  def this() = this(Module.defaultLogLevel)
  override def configure(): Unit = {
    install(Modules.`override`(RealModule).`with`(ExistingAlbumsModules.lazyAlbums))
    install(RealInternetTalkerModule.nonDaemonic)
    setScribeLogging()
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

private object Module {
  /** Should only be used for testing! Set `None` for no logs. */
  private[controllers] var defaultLogLevel: Option[Level] = Some(Level.Debug)
}
