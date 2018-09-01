package common

import com.google.inject.{Binder, Provider}
import com.google.inject.assistedinject.FactoryModuleBuilder
import net.codingwell.scalaguice.{typeLiteral, InternalModule}

//noinspection UnitMethodIsParameterless
trait ModuleUtils {self: InternalModule[_ <: Binder] =>
  private def binder: Binder = self.binderAccess

  protected def install[Factory: Manifest]: Unit = {
    binder.install(new FactoryModuleBuilder().build(manifest.runtimeClass))
  }

  private def unerasedClass[A: Manifest]: Class[A] = manifest.runtimeClass.asInstanceOf[Class[A]]
  protected def install[Source: Manifest, Target <: Source : Manifest, Factory: Manifest]: Unit = {
    binder.install(new FactoryModuleBuilder()
        .implement(
          typeLiteral[Source],
          typeLiteral[Target])
        .build(unerasedClass[Factory]))
  }

  protected def requireBinding[A: Manifest]: Unit = binder.getProvider(manifest.runtimeClass)

  protected def provider[A: Manifest]: Provider[A] = binder.getProvider(unerasedClass[A])
}