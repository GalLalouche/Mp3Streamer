package controllers.websockets

import com.google.inject.ImplementedBy

@ImplementedBy(classOf[WebSocketRegistryFactoryImpl])
trait PlayWebSocketRegistryFactory {
  def apply(name: String): PlayWebSocketRef
}
