package http4s

import formatter.FormatterModule
import http4s.routes.RoutesModule
import net.codingwell.scalaguice.ScalaModule

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.fromExecutorService
import scala.concurrent.duration.DurationInt

import common.concurrency.ElasticExecutor

private object Http4sModule extends ScalaModule {
  override def configure(): Unit = {
    install(new FormatterModule)
    install(RoutesModule)

    val processors = Runtime.getRuntime.availableProcessors
    val executor = ElasticExecutor("Http4s Main", 2.minutes, bound = processors, daemon = false)
    bind[ExecutionContext].toInstance(fromExecutorService(executor))
  }
}
