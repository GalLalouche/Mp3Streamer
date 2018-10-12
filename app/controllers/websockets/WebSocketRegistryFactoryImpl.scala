package controllers.websockets

import java.util.concurrent.ConcurrentHashMap

import backend.logging.Logger
import javax.inject.{Inject, Singleton}
import common.rich.collections.RichMap._

@Singleton
private class WebSocketRegistryFactoryImpl @Inject()(logger: Logger) extends PlayWebSocketRegistryFactory {
  private val registries = new ConcurrentHashMap[String, PlayWebSocketRef]
  override def apply(name: String): PlayWebSocketRef =
    registries.getOrPutIfAbsent(name, new WebSocketRegistryImpl(logger, name))
}
