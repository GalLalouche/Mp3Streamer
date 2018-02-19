package backend.albums

import java.util.concurrent.Semaphore

import backend.configs.StandaloneConfig
import backend.logging.LoggingLevel
import common.io.WSAliases.WSClient
import common.rich.RichFuture
import common.rich.RichT._

import scala.concurrent.{ExecutionContext, Future}

// Ensures MusicBrainz aren't flooded since:
// 1. At most 3 WS clients are alive (semaphores).
// 2. A request for a client has a 1 second delay.
private object NewAlbumsConfig extends StandaloneConfig {
  logger.setCurrentLevel(LoggingLevel.Verbose)
  private val semaphore = new Semaphore(3)
  private val semaphoreReleasingService: ExecutionContext = CurrentThreadExecutionContext
  override def useWs[T](f: WSClient => Future[T]): Future[T] = {
    semaphore.acquire()
    RichFuture.richFuture(super.useWs(f))(semaphoreReleasingService) consumeTry semaphore.release().const
  }
  override protected def createWsClient() = {
    Thread sleep 1000
    super.createWsClient()
  }
}
