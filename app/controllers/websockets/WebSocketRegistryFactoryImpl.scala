package controllers.websockets

import java.util.concurrent.ConcurrentHashMap

import backend.logging.Logger
import javax.inject.{Inject, Singleton}
import common.rich.collections.RichMap._

@Singleton
private class WebSocketRegistryFactoryImpl @Inject()(logger: Logger) extends WebSocketRegistryFactory {
  private val registries = new ConcurrentHashMap[String, WebSocketRegistry]
  override def apply(name: String): WebSocketRegistry =
    registries.getOrPutIfAbsent(name, new WebSocketRegistryImpl(logger, name))
}
