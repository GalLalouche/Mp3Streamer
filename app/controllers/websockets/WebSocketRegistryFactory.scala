package controllers.websockets

import com.google.inject.ImplementedBy

@ImplementedBy(classOf[WebSocketRegistryFactoryImpl])
trait WebSocketRegistryFactory {
  def apply(name: String): WebSocketRegistry
}
