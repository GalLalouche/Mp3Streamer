package backend.configs

import com.google.inject.Guice
import com.google.inject.util.Modules

trait StandaloneConfig extends Configuration {
  override val module = Modules `override` RealModule `with` StandaloneModule
  override val injector = Guice createInjector module
}

object StandaloneConfig extends StandaloneConfig
