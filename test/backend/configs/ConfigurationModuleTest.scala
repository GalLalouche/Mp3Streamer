package backend.configs

import controllers.ControllerUtils

class ConfigurationModuleTest extends ConfigurationModuleTestSpec {
  allRequiredBindingsSatisfied("ControllerConfiguration", ControllerUtils.config.injector)
}
