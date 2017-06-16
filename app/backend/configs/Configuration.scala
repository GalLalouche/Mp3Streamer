package backend.configs

import backend.logging.Logger
import common.io.{DirectoryRef, InternetTalker}
import models.MusicFinder
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext

trait Configuration extends ExecutionContext with InternetTalker { self =>
  type D <: DirectoryRef {type D = self.D}
  implicit val ec: ExecutionContext
  override def execute(runnable: Runnable): Unit = ec execute runnable
  override def reportFailure(cause: Throwable): Unit = ec reportFailure cause
  implicit val driver: JdbcProfile
  implicit val db: driver.backend.DatabaseDef
  implicit val mf: MusicFinder { type D = self.D}
  implicit val rootDirectory: DirectoryRef
  implicit val logger: Logger
}
