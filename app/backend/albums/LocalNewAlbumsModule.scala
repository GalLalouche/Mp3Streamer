package backend.albums

import java.util.concurrent.Semaphore

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import backend.Retriever
import backend.module.{AllModules, RealModule}
import backend.logging.{ConsoleLogger, FilteringLogger, Logger}
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
private object LocalNewAlbumsModule extends ScalaModule with ModuleUtils {
  private val semaphore = new Semaphore(3)
  private val semaphoreReleasingContext: ExecutionContext = CurrentThreadExecutionContext
  override def configure(): Unit = {
    // TODO Extract to LoggerModule since this config is used by StandaloneModule as well
    bind[Logger] toInstance new ConsoleLogger with FilteringLogger
    bind[ExecutionContext] toInstance semaphoreReleasingContext
    bind[InternetTalker] toInstance new InternetTalker {
      override def execute(runnable: Runnable) = semaphoreReleasingContext.execute(runnable)
      override def reportFailure(cause: Throwable) = semaphoreReleasingContext.reportFailure(cause)
      override protected def createWsClient() = {
        Thread sleep 1000
        StandaloneAhcWSClient()(ActorMaterializer()(ActorSystem.create("NewAlbumsModule-System")))
      }
      override def useWs[T](f: Retriever[WSClient, T]) = {
        semaphore.acquire()
        RichFuture.richFuture(super.useWs(f))(semaphoreReleasingContext) consumeTry semaphore.release().const
      }
    }

    install[NewAlbumsRetrieverFactory]
    install(RealModule)
    install(AllModules)
  }
}
