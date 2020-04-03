package backend.albums.filler

import java.util.concurrent.Semaphore

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import backend.Retriever
import backend.logging.{Logger, LoggingModules}
import backend.module.RealInternetTalkerModule
import com.google.inject.{Provides, Singleton}
import models.{IOMusicFinder, IOMusicFinderModule, MusicFinder}
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.ExecutionContext

import common.concurrency.SingleThreadedJobQueue
import common.io.InternetTalker
import common.io.WSAliases.WSClient
import common.rich.RichFuture

// Ensures MusicBrainz aren't flooded since:
// 1. A limited number of WS can be used at any given time (semaphores).
// 2. A request for a client has a 1 second delay.
private object LocalNewAlbumsModule extends ScalaModule {
  override def configure(): Unit = {
    bind[ExecutionContext] toInstance ExecutionContext.Implicits.global

    install(LoggingModules.ConsoleWithFiltering)
    install(new IOMusicFinderModule(
      new IOMusicFinder {
        override val subDirNames: List[String] = List("Rock", "Metal")
      }
    ))
  }

  @Provides
  @Singleton
  private def internetTalker(ec: ExecutionContext): InternetTalker = new InternetTalker {
    private val am = ActorMaterializer()(
      ActorSystem.create("NewAlbumsModule-System", RealInternetTalkerModule.warningOnlyDaemonicConfig))

    private val internetTalkingThread = new SingleThreadedJobQueue("InternetTalkingThread").asExecutionContext
    override def execute(runnable: Runnable) = internetTalkingThread.execute(runnable)
    override def reportFailure(cause: Throwable) = internetTalkingThread.reportFailure(cause)

    private val semaphore = new Semaphore(1)
    private val semaphoreReleasingContext = new SingleThreadedJobQueue("SemaphoreReleasingContext")

    override protected def createWsClient() = StandaloneAhcWSClient()(am)
    override def useWs[T](f: Retriever[WSClient, T]) = {
      semaphore.acquire()
      // As an extra precaution of arguable efficacy, all semaphores are released by a single thread.
      RichFuture.richFuture(super.useWs(f))(internetTalkingThread)
          .consumeTry(_ => semaphoreReleasingContext {
            Thread sleep 1000
            semaphore.release()
          })
    }
  }

  @Provides
  @Singleton
  private def existingAlbumsCache(mf: MusicFinder, logger: Logger): ExistingAlbums = {
    logger.verbose("Creating cache")
    ExistingAlbums.from(mf.genreDirs.view
        .flatMap(_.deepDirs)
        .flatMap(NewAlbumsRetriever.dirToAlbum(_, mf))
        .toVector
    )
  }
}
