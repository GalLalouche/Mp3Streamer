package backend.albums.gui

import backend.logging.{FilteringLogger, LoggingLevel}
import backend.module.StandaloneModule
import com.google.inject.Guice
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector

import scalafx.application.JFXApp3
import scalafx.scene.Scene

private object GuiApp extends JFXApp3 {
  override def start() = {
    val injector = Guice.createInjector(StandaloneModule)
    injector.instance[FilteringLogger].setCurrentLevel(LoggingLevel.Debug)
    val aux = injector.instance[NewAlbumsGui]
    stage = new JFXApp3.PrimaryStage {
      title.value = "New Albums"
      width = 3000
      height = 1400
      scene = new Scene {
        root = aux.root
      }
    }
  }
}
