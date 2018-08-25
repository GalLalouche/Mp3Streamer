package backend.configs

import com.google.inject.{Injector, Module}
import common.io.InternetTalker

import scala.concurrent.ExecutionContext

trait Configuration extends InternetTalker {
  // The module and injector should really be vals (or lazy-vals) in every concrete implementation.
  protected def module: Module
  def injector: Injector
  protected def ec: ExecutionContext
  override def execute(runnable: Runnable): Unit = ec execute runnable
  override def reportFailure(cause: Throwable): Unit = ec reportFailure cause
}
