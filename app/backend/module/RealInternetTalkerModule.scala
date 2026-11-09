package backend.module

import com.google.inject.Provides
import net.codingwell.scalaguice.ScalaModule

import cats.effect.unsafe.{IORuntime, IORuntimeConfig}

import common.io.WSAliases.WSClient

object RealInternetTalkerModule extends ScalaModule {
  @Provides private def provideWSClient(): WSClient = Http4sWSClient(runtime)

  private lazy val runtime: IORuntime = {
    val (compute, shutdownCompute) =
      IORuntime.createWorkStealingComputeThreadPool(threadPrefix = "ember-ws-compute")
    val (blocking, shutdownBlocking) =
      IORuntime.createDefaultBlockingExecutionContext("ember-ws-blocking")
    val (scheduler, shutdownScheduler) = IORuntime.createDefaultScheduler("ember-ws-scheduler")
    val shutdown = () => { shutdownCompute(); shutdownBlocking(); shutdownScheduler() }
    IORuntime(compute, blocking, scheduler, shutdown, IORuntimeConfig())
  }
}
