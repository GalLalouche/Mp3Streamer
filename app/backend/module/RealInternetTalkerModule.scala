package backend.module

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.google.inject.{Module, Provides}
import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}
import common.ModuleUtils
import common.io.InternetTalker
import common.io.WSAliases.WSClient
import net.codingwell.scalaguice.ScalaPrivateModule
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.ExecutionContext

private class RealInternetTalkerModule private(am: ActorMaterializer) extends ScalaPrivateModule with ModuleUtils {
  override def configure(): Unit = {
    requireBinding[ExecutionContext]
    bind[ActorMaterializer] toInstance am

    requireBinding[ExecutionContext]
    expose[InternetTalker]
  }

  @Provides
  private def provideInternetTalker(
      _ec: ExecutionContext, materializer: ActorMaterializer): InternetTalker = new InternetTalker {
    override def execute(runnable: Runnable) = _ec.execute(runnable)
    override def reportFailure(cause: Throwable) = _ec.reportFailure(cause)
    override protected def createWsClient(): WSClient = StandaloneAhcWSClient()(materializer)
  }
}

object RealInternetTalkerModule {
  val warningOnlyConfig: Config = ConfigFactory.load()
      .withValue("akka.loglevel", ConfigValueFactory.fromAnyRef("WARNING"))
      .withValue("akka.stdout-loglevel", ConfigValueFactory.fromAnyRef("WARNING"))
  def daemonic: Module = new RealInternetTalkerModule(
    ActorMaterializer()(ActorSystem.create(
      "Standalone-Config-WS-System", warningOnlyConfig
          .withValue("akka.daemonic", ConfigValueFactory.fromAnyRef(true)))))

  def nonDaemonic: Module = new RealInternetTalkerModule(
    ActorMaterializer()(ActorSystem.create("RealConfigWS-System", warningOnlyConfig)))
}
