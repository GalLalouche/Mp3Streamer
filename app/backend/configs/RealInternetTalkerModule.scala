package backend.configs

import akka.actor.ActorSystem
import scala.collection.JavaConverters._
import akka.stream.ActorMaterializer
import com.google.inject.{Module, Provides}
import com.typesafe.config.ConfigFactory
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

private object RealInternetTalkerModule {
  def daemonic: Module = new RealInternetTalkerModule(
    ActorMaterializer()(ActorSystem.create(
      "Standalone-Config-WS-System",
      ConfigFactory.parseMap(Map("akka.daemonic" -> true).asJava)))
  )

  def nonDaemonic: Module =
    new RealInternetTalkerModule(ActorMaterializer()(ActorSystem.create("RealConfigWS-System")))
}
