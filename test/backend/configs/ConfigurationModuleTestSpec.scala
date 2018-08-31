package backend.configs

import com.google.inject.Injector
import org.scalatest.{FreeSpec, Matchers}

class ConfigurationModuleTestSpec extends FreeSpec with Matchers {
  protected def allRequiredBindingsSatisfied(configName: String, i: => Injector): Unit = configName in {
    noException shouldBe thrownBy {i}
  }
}
