package controllers.websockets

import java.util.concurrent.ConcurrentHashMap
import javax.inject.Singleton

import common.rich.collections.RichMap._

@Singleton
private class WebSocketRegistryFactoryImpl extends PlayWebSocketRegistryFactory {
  private val registries = new ConcurrentHashMap[String, PlayWebSocketRef]
  override def apply(name: String): PlayWebSocketRef =
    registries.getOrPutIfAbsent(name, new WebSocketRegistryImpl(name))
}
