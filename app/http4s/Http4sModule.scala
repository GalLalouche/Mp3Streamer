package http4s

import java.util.concurrent.Executors.newFixedThreadPool

import controllers.ServerModule
import net.codingwell.scalaguice.ScalaModule

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.fromExecutorService

private object Http4sModule extends ScalaModule {
  override def configure(): Unit = {
    install(new ServerModule)
    val processors = Runtime.getRuntime.availableProcessors
    bind[ExecutionContext].toInstance(fromExecutorService(newFixedThreadPool(processors * 2)))
  }
}
