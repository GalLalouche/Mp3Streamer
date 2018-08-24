package backend.configs

import com.google.inject.Injector
import controllers.ControllerUtils
import org.scalatest.{FreeSpec, Matchers}

class ConfigurationModuleTest extends FreeSpec with Matchers {
  private def allRequiredBindingsSatisfied(configName: String, i: => Injector): Unit = configName in {
    noException shouldBe thrownBy {i}
  }

  allRequiredBindingsSatisfied("StandaloneConfig", StandaloneConfig.injector)
  allRequiredBindingsSatisfied("CleanConfiguration", CleanConfiguration.injector)
  allRequiredBindingsSatisfied("ControllerConfiguration", ControllerUtils.config.injector)
}
