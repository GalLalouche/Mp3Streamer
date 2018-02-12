package backend.configs

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import backend.logging._
import com.typesafe.config.ConfigFactory
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext

trait StandaloneConfig extends RealConfig {
  override implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  override implicit val logger: FilteringLogger = new ConsoleLogger with FilteringLogger
  private lazy val materializer =
    ActorMaterializer()(ActorSystem.create("Standalone-Config-WS-System",
      ConfigFactory.parseMap(Map("akka.daemonic" -> true).asJava)))
  override protected def createWsClient() = StandaloneAhcWSClient()(materializer)
}

object StandaloneConfig extends StandaloneConfig
