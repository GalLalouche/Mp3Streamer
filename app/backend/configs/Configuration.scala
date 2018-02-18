package backend.configs

import java.time.Clock

import backend.logging.{Logger, LoggerProvider}
import backend.storage.DbProvider
import common.io.{DirectoryRef, InternetTalker, RootDirectoryProvider}
import models.{MusicFinder, MusicFinderProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

trait Configuration extends InternetTalker with LoggerProvider with DbProvider with MusicFinderProvider
    with RootDirectoryProvider with ClockProvider {
  protected def ec: ExecutionContext
  override def execute(runnable: Runnable): Unit = ec execute runnable
  override def reportFailure(cause: Throwable): Unit = ec reportFailure cause
  override val profile: JdbcProfile
  override val db: profile.backend.Database
  override val mf: MusicFinder
  override val rootDirectory: DirectoryRef
  override val logger: Logger
  override val clock: Clock
}
