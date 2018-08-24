package backend.configs

import backend.logging.{Logger, LoggerProvider}
import backend.storage.DbProvider
import com.google.inject.Injector
import common.io.{DirectoryRef, InternetTalker, RootDirectoryProvider}
import models.{MusicFinder, MusicFinderProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

trait Configuration extends InternetTalker with LoggerProvider with DbProvider with MusicFinderProvider
    with RootDirectoryProvider {
  def injector: Injector
  protected def ec: ExecutionContext
  override def execute(runnable: Runnable): Unit = ec execute runnable
  override def reportFailure(cause: Throwable): Unit = ec reportFailure cause
  override val profile: JdbcProfile
  override def db: profile.backend.Database
  // TODO figure out why this has to be a val :|
  override val mf: MusicFinder
  override def rootDirectory: DirectoryRef
  override def logger: Logger
}
