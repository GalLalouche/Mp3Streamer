package backend.albums

import java.util.concurrent.Semaphore

import backend.Retriever
import backend.configs.StandaloneConfig
import com.google.inject.Guice
import common.io.WSAliases.WSClient
import common.rich.RichFuture
import common.rich.RichT._

import scala.concurrent.{ExecutionContext, Future}

// Ensures MusicBrainz aren't flooded since:
// 1. At most 3 WS clients are alive (semaphores).
// 2. A request for a client has a 1 second delay.
private object NewAlbumsConfig extends StandaloneConfig {
  override val injector = Guice createInjector module
  private val semaphore = new Semaphore(3)
  private val semaphoreReleasingService: ExecutionContext = CurrentThreadExecutionContext
  override def useWs[T](f: Retriever[WSClient, T]): Future[T] = {
    semaphore.acquire()
    RichFuture.richFuture(super.useWs(f))(semaphoreReleasingService) consumeTry semaphore.release().const
  }
  override protected def createWsClient() = {
    Thread sleep 1000
    super.createWsClient()
  }
}
