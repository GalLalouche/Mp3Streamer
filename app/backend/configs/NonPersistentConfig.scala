package backend.configs

import com.google.inject.Module

trait NonPersistentConfig extends Configuration {
  override def module: Module = NonPersistentModule
}
