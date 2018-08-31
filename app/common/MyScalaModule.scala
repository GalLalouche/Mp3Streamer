package common

import com.google.inject.assistedinject.FactoryModuleBuilder
import net.codingwell.scalaguice.ScalaModule

//noinspection UnitMethodIsParameterless
// TODO pull request?
// Needs to be a class otherwise a compilation error occurs. No idea how codingwell solved it :\
abstract class MyScalaModule extends ScalaModule {
  protected def install[Factory: Manifest]: Unit = {
    install(new FactoryModuleBuilder().build(manifest.runtimeClass))
  }
}
