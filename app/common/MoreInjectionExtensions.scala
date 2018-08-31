package common

import com.google.inject.{Injector, Provider}

//noinspection UnitMethodIsParameterless
object MoreInjectionExtensions {
  private def unerasedClass[A: Manifest]: Class[A] = manifest.runtimeClass.asInstanceOf[Class[A]]
  implicit class RichInjector(private val $: Injector) extends AnyVal {
    def provider[A: Manifest]: Provider[A] = $.getProvider(unerasedClass[A])
  }
}
