package backend.scorer.utils.foobar

import backend.module.StandaloneModule
import better.files
import better.files.FileMonitor
import com.google.inject.Guice
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector

import java.io.File
import scala.concurrent.ExecutionContext

import javafx.stage.WindowEvent
import scalafx.application.{JFXApp3, Platform}
import scalafx.scene.Scene

import common.rich.RichFuture.richFuture

/** Runs a small GUI for scoring the currently playing Foobar song. */
private object GuiApp extends JFXApp3 {
  private val FileDump = new File("""D:\Media-temp\streamer\now_playing.txt""")
  override def start() = {
    val injector = Guice.createInjector(StandaloneModule)
    implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
    val aux = injector.instance[FoobarScorer]
    def update(): Unit =
    // TODO safeForeach in MonadError or RichFuture?
      aux.update(FileDump, () => update()).toTry.foreach(newRoot => Platform.runLater(stage.scene.get.setRoot(newRoot.get)))
    val monitor = new FileMonitor(better.files.File(FileDump.toPath)) {
      override def onModify(file: files.File, count: Int): Unit = update()
    }
    stage = new JFXApp3.PrimaryStage {
      title.value = "Foobar Scorer"
      width = 800
      height = 210
      scene = new Scene
      onCloseRequest = (_: WindowEvent) => {
        println("Closing!")
        monitor.close()
      }
    }
    monitor.start()
    update()
  }
}
