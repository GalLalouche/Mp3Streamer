package backend.configs

import com.google.inject.{Injector, Module}

trait Configuration {
  // The module and injector should really be vals (or lazy-vals) in every concrete implementation.
  def module: Module
  def injector: Injector
}
