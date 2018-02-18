package backend.configs

import java.time.Clock

import backend.logging.{Logger, LoggerProvider}
import backend.storage.DbProvider
import common.io.{DirectoryRef, InternetTalker, RootDirectoryProvider}
import models.{MusicFinder, MusicFinderProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

trait Configuration extends InternetTalker with LoggerProvider with DbProvider with MusicFinderProvider
    with RootDirectoryProvider {
  implicit val ec: ExecutionContext
  override def execute(runnable: Runnable): Unit = ec execute runnable
  override def reportFailure(cause: Throwable): Unit = ec reportFailure cause
  override implicit val profile: JdbcProfile
  override implicit val db: profile.backend.Database
  override implicit val mf: MusicFinder
  override implicit val rootDirectory: DirectoryRef
  override implicit val logger: Logger
  implicit val clock: Clock
}
