package controllers.websockets

import java.util.concurrent.ConcurrentHashMap
import javax.inject.{Inject, Singleton}

import backend.logging.Logger
import common.rich.collections.RichMap._

@Singleton
private class WebSocketRegistryFactoryImpl @Inject() (logger: Logger)
    extends PlayWebSocketRegistryFactory {
  private val registries = new ConcurrentHashMap[String, PlayWebSocketRef]
  override def apply(name: String): PlayWebSocketRef =
    registries.getOrPutIfAbsent(name, new WebSocketRegistryImpl(logger, name))
}
