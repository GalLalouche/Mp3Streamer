package backend.albums

import backend.configs.StandaloneConfig
import com.google.inject.Guice
import com.google.inject.util.Modules

private object NewAlbumsConfig extends StandaloneConfig {
  override val injector = Guice createInjector (Modules `override` module `with` NewAlbumsModule)
}
