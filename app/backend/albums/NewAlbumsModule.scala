package backend.albums

import java.util.concurrent.Semaphore

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import backend.Retriever
import backend.configs.AllModules
import common.ModuleUtils
import common.io.InternetTalker
import common.io.WSAliases.WSClient
import common.rich.RichFuture
import common.rich.RichT._
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.ExecutionContext

// Ensures MusicBrainz aren't flooded since:
// 1. At most 3 WS clients are alive (semaphores).
// 2. A request for a client has a 1 second delay.
private object NewAlbumsModule extends ScalaModule with ModuleUtils {
  private val semaphore = new Semaphore(3)
  private val semaphoreReleasingService: ExecutionContext = CurrentThreadExecutionContext
  // TODO handle code duplication with RealModule
  private lazy val materializer = ActorMaterializer()(ActorSystem.create("NewAlbumsModule-System"))
  override def configure(): Unit = {
    bind[InternetTalker] toInstance new InternetTalker {
      override def execute(runnable: Runnable) = semaphoreReleasingService.execute(runnable)
      override def reportFailure(cause: Throwable) = semaphoreReleasingService.reportFailure(cause)
      override protected def createWsClient() = {
        Thread sleep 1000
        StandaloneAhcWSClient()(materializer)
      }
      override def useWs[T](f: Retriever[WSClient, T]) = {
        semaphore.acquire()
        RichFuture.richFuture(super.useWs(f))(semaphoreReleasingService) consumeTry semaphore.release().const
      }
    }

    install[NewAlbumsRetrieverFactory]
    install(AllModules)
  }
}
