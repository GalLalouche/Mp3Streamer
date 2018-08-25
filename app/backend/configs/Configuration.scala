package backend.configs

import com.google.inject.{Injector, Module}
import common.io.InternetTalker

trait Configuration extends InternetTalker {
  // The module and injector should really be vals (or lazy-vals) in every concrete implementation.
  protected def module: Module
  def injector: Injector
}
