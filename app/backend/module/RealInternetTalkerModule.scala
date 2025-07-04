package backend.module

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.google.inject.{Module, Provides}
import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import common.io.WSAliases.WSClient

private class RealInternetTalkerModule private (am: Materializer) extends ScalaModule {
  @Provides private def provideWSClient(): WSClient = StandaloneAhcWSClient()(am)
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
