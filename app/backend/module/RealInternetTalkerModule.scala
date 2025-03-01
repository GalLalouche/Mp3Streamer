package backend.module

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.google.inject.{Module, Provides}
import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}
import net.codingwell.scalaguice.ScalaPrivateModule
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.ExecutionContext

import common.guice.ModuleUtils
import common.io.InternetTalker
import common.io.WSAliases.WSClient

private class RealInternetTalkerModule private (am: Materializer)
    extends ScalaPrivateModule
    with ModuleUtils {
  override def configure(): Unit = {
    requireBinding[ExecutionContext]
    bind[Materializer].toInstance(am)

    requireBinding[ExecutionContext]
    expose[InternetTalker]
  }

  @Provides private def internetTalker(
      _ec: ExecutionContext,
      materializer: Materializer,
  ): InternetTalker = new InternetTalker {
    override def execute(runnable: Runnable) = _ec.execute(runnable)
    override def reportFailure(cause: Throwable) = _ec.reportFailure(cause)
    protected override def createWsClient(): WSClient = StandaloneAhcWSClient()(materializer)
  }
}

object RealInternetTalkerModule {
  private val warningOnlyConfig: Config = ConfigFactory
    .load()
    .withValue("akka.loglevel", ConfigValueFactory.fromAnyRef("WARNING"))
    .withValue("akka.stdout-loglevel", ConfigValueFactory.fromAnyRef("WARNING"))
  val warningOnlyDaemonicConfig: Config = warningOnlyConfig
    .withValue("akka.daemonic", ConfigValueFactory.fromAnyRef(true))
  def daemonic: Module = new RealInternetTalkerModule(
    Materializer(
      ActorSystem.create("Standalone-Config-WS-System", warningOnlyDaemonicConfig),
    ),
  )

  def nonDaemonic: Module = new RealInternetTalkerModule(
    Materializer(ActorSystem.create("RealConfigWS-System", warningOnlyConfig)),
  )
}
