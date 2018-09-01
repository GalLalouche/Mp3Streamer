package backend.albums

import backend.configs.{Configuration, StandaloneModule}
import com.google.inject.Guice
import com.google.inject.util.Modules

private object NewAlbumsConfig extends Configuration {
  override val module = NewAlbumsModule
  override val injector = Guice createInjector (Modules `override` StandaloneModule `with` module)
}
