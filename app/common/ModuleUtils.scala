package common

import com.google.inject.assistedinject.FactoryModuleBuilder
import com.google.inject.spi.{InjectionListener, TypeEncounter, TypeListener}
import com.google.inject.{Binder, Provider, TypeLiteral}
import common.rich.primitives.RichClass._
import net.codingwell.scalaguice.{typeLiteral, InternalModule}

//noinspection UnitMethodIsParameterless
trait ModuleUtils {self: InternalModule[_ <: Binder] =>
  private def binder: Binder = self.binderAccess

  protected def install[Factory: Manifest]: Unit = {
    binder.install(new FactoryModuleBuilder().build(manifest.runtimeClass))
  }

  protected def install[Source: Manifest, Target <: Source : Manifest, Factory: Manifest]: Unit = {
    binder.install(new FactoryModuleBuilder()
        .implement(typeLiteral[Source], typeLiteral[Target])
        .build(manifest[Factory].unerasedClass))
  }

  protected def requireBinding[A: Manifest]: Unit = binder.getProvider(manifest.runtimeClass)

  protected def provider[A: Manifest]: Provider[A] = binder.getProvider(manifest.unerasedClass)

  def typeListener[A: Manifest](f: A => Any): TypeListener with InjectionListener[A] =
    new TypeListener with InjectionListener[A] {
      import common.rich.primitives.RichClass._

      override def hear[I](`type`: TypeLiteral[I], encounter: TypeEncounter[I]): Unit = {
        if (`type`.getRawType.isAssignableTo(manifest.runtimeClass))
          encounter.register(this.asInstanceOf[InjectionListener[I]])
      }
      override def afterInjection(injectee: A) = f(injectee)
    }
}
