package controllers.websockets

import java.util.concurrent.ConcurrentHashMap

import backend.logging.Logger
import javax.inject.{Inject, Singleton}

@Singleton
private class WebSocketRegistryFactoryImpl @Inject()(logger: Logger) extends WebSocketRegistryFactory {
  private val registries = new ConcurrentHashMap[String, WebSocketRegistry]
  override def apply(name: String): WebSocketRegistry = {
    // TODO get or put if absent
    registries.putIfAbsent(name, new WebSocketRegistryImpl(logger, name))
    registries.get(name)
  }
}
