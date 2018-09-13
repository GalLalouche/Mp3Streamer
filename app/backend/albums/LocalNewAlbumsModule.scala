package backend.albums

import java.util.concurrent.Semaphore

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import backend.Retriever
import backend.logging.LoggingModules
import backend.module.{AllModules, RealModule}
import common.rich.RichT._
import common.ModuleUtils
import common.concurrency.SingleThreadedJobQueue
import common.io.InternetTalker
import common.io.WSAliases.WSClient
import common.rich.RichFuture
import models.IOMusicFinder
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.ExecutionContext

// Ensures MusicBrainz aren't flooded since:
// 1. At most 3 WS clients are alive (semaphores).
// 2. A request for a client has a 1 second delay.
private object LocalNewAlbumsModule extends ScalaModule with ModuleUtils {
  private val it: InternetTalker = new InternetTalker {
    private val am = ActorMaterializer()(ActorSystem.create("NewAlbumsModule-System"))

    override def execute(runnable: Runnable) = semaphoreReleasingContext.execute(runnable)
    override def reportFailure(cause: Throwable) = semaphoreReleasingContext.reportFailure(cause)

    private val semaphore = new Semaphore(3)
    private val semaphoreReleasingContext: ExecutionContext =
      new SingleThreadedJobQueue("LocalNewAlbumsModule").asExecutionContext

    override protected def createWsClient() = {
      Thread sleep 1000
      StandaloneAhcWSClient()(am)
    }
    override def useWs[T](f: Retriever[WSClient, T]) = {
      semaphore.acquire()
      // As an extra precaution of arguable efficacy, all semaphores are released by a single thread.
      RichFuture.richFuture(super.useWs(f))(semaphoreReleasingContext)
          .consumeTry(semaphoreReleasingContext.execute(() => semaphore.release()).const)
    }
  }

  override def configure(): Unit = {
    bind[ExecutionContext] toInstance ExecutionContext.Implicits.global
    bind[InternetTalker] toInstance it
    bind[IOMusicFinder] toInstance new IOMusicFinder {
      override val subDirNames: List[String] = List("Rock", "Metal")
    }
    install[NewAlbumsRetrieverFactory]

    install(LoggingModules.ConsoleWithFiltering)
  }
}
