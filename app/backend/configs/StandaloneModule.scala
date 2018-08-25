package backend.configs

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import backend.logging.{ConsoleLogger, FilteringLogger, Logger}
import com.typesafe.config.ConfigFactory
import net.codingwell.scalaguice.ScalaModule

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext

object StandaloneModule extends ScalaModule {
  override def configure(): Unit = {
    bind[Logger] toInstance new ConsoleLogger with FilteringLogger
    bind[ExecutionContext] toInstance ExecutionContext.Implicits.global
    bind[ActorMaterializer] toInstance ActorMaterializer()(ActorSystem.create(
      "Standalone-Config-WS-System",
      ConfigFactory.parseMap(Map("akka.daemonic" -> true).asJava)))
  }
}
