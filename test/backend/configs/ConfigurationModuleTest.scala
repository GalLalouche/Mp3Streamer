package backend.configs

import controllers.ControllerUtils

class ConfigurationModuleTest extends ConfigurationModuleTestSpec {
  allRequiredBindingsSatisfied("StandaloneConfig", StandaloneConfig.injector)
  allRequiredBindingsSatisfied("CleanConfiguration", CleanConfiguration.injector)
  allRequiredBindingsSatisfied("ControllerConfiguration", ControllerUtils.config.injector)
}
