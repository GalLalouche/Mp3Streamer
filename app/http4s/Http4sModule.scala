package http4s

import java.util.concurrent.Executors.newFixedThreadPool

import formatter.FormatterModule
import http4s.routes.RoutesModule
import net.codingwell.scalaguice.ScalaModule

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.fromExecutorService

private object Http4sModule extends ScalaModule {
  override def configure(): Unit = {
    install(new FormatterModule)
    install(RoutesModule)
    val processors = Runtime.getRuntime.availableProcessors
    bind[ExecutionContext].toInstance(fromExecutorService(newFixedThreadPool(processors * 2)))
  }
}
