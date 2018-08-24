package backend.configs

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.google.inject.Guice
import com.google.inject.util.Modules
import com.typesafe.config.ConfigFactory
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext

trait StandaloneConfig extends RealConfig {
  override protected val ec: ExecutionContext = ExecutionContext.Implicits.global
  private lazy val materializer =
    ActorMaterializer()(ActorSystem.create("Standalone-Config-WS-System",
      ConfigFactory.parseMap(Map("akka.daemonic" -> true).asJava)))
  override val module = Modules.`override`(super.module).`with`(StandaloneModule)
  override protected def createWsClient() = StandaloneAhcWSClient()(materializer)
}

object StandaloneConfig extends StandaloneConfig {
  override val injector = Guice createInjector module
}
