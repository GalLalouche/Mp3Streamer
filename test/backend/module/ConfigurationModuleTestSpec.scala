package backend.module

import com.google.inject.{Guice, Injector, Module}
import org.scalatest.{FreeSpec, Matchers}

class ConfigurationModuleTestSpec extends FreeSpec with Matchers {
  protected def allRequiredBindingsSatisfied(configName: String, i: => Injector): Unit = configName in {
    noException shouldBe thrownBy {i}
  }

  protected def allRequiredBindingsSatisfied(configName: String, m: Module): Unit =
    allRequiredBindingsSatisfied(configName, Guice createInjector m)
}
