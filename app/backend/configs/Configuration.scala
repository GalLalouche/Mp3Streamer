package backend.configs

import backend.storage.DbProvider
import com.google.inject.{Injector, Module}
import common.io.InternetTalker
import models.{MusicFinder, MusicFinderProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

trait Configuration extends InternetTalker with DbProvider with MusicFinderProvider {
  // The module and injector should really be vals (or lazy-vals) in every concrete implementation.
  protected def module: Module
  def injector: Injector
  protected def ec: ExecutionContext
  override def execute(runnable: Runnable): Unit = ec execute runnable
  override def reportFailure(cause: Throwable): Unit = ec reportFailure cause
  override val profile: JdbcProfile
  override def db: profile.backend.Database
  // TODO figure out why this has to be a val :|
  override val mf: MusicFinder
}
