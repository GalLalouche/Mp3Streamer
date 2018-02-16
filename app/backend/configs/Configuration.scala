package backend.configs

import java.time.Clock

import backend.logging.{Logger, LoggerProvider}
import backend.storage.DbProvider
import common.io.{DirectoryRef, InternetTalker}
import models.MusicFinder
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

trait Configuration extends InternetTalker with LoggerProvider with DbProvider {
  implicit val ec: ExecutionContext
  override def execute(runnable: Runnable): Unit = ec execute runnable
  override def reportFailure(cause: Throwable): Unit = ec reportFailure cause
  override implicit val profile: JdbcProfile
  override implicit val db: profile.backend.Database
  implicit val mf: MusicFinder
  implicit val rootDirectory: DirectoryRef
  override implicit val logger: Logger
  implicit val clock: Clock
}
