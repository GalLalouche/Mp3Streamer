package backend.albums

import backend.configs.ConfigurationModuleTestSpec

class NewAlbumsModuleTest extends ConfigurationModuleTestSpec {
  allRequiredBindingsSatisfied("NewAlbumsConfig", NewAlbumsConfig.injector)
}
