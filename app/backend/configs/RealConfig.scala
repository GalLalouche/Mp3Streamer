package backend.configs

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.google.inject.Module
import play.api.libs.ws.ahc.StandaloneAhcWSClient

trait RealConfig extends Configuration {
  private lazy val materializer = ActorMaterializer()(ActorSystem.create("RealConfigWS-System"))

  override def module: Module = RealModule
  override protected def createWsClient() = StandaloneAhcWSClient()(materializer)
}
