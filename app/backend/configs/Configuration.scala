package backend.configs

import java.time.Clock

import backend.logging.Logger
import common.io.{DirectoryRef, InternetTalker}
import models.MusicFinder
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

trait Configuration extends InternetTalker {
  implicit val ec: ExecutionContext
  override def execute(runnable: Runnable): Unit = ec execute runnable
  override def reportFailure(cause: Throwable): Unit = ec reportFailure cause
  implicit val profile: JdbcProfile
  implicit val db: profile.backend.Database
  implicit val mf: MusicFinder
  implicit val rootDirectory: DirectoryRef
  override implicit val logger: Logger
  implicit val clock: Clock
}
