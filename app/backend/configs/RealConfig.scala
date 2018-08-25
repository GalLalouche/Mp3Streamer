package backend.configs

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.google.inject.Module
import play.api.libs.ws.ahc.StandaloneAhcWSClient

trait RealConfig extends Configuration {
  override def module: Module = RealModule

}
