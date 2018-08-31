package common

import com.google.inject.Binder
import com.google.inject.assistedinject.FactoryModuleBuilder
import net.codingwell.scalaguice.InternalModule

//noinspection UnitMethodIsParameterless
trait ModuleUtils {self: InternalModule[_ <: Binder] =>
  private def binder: Binder = self.binderAccess

  protected def install[Factory: Manifest]: Unit = {
    binder.install(new FactoryModuleBuilder().build(manifest.runtimeClass))
  }

  protected def requireBinding[A: Manifest]: Unit = binder.getProvider(manifest.runtimeClass)
}
