package backend.albums

import backend.module.ConfigurationModuleTestSpec

class NewAlbumsModuleTest extends ConfigurationModuleTestSpec {
  allRequiredBindingsSatisfied("NewAlbumsConfig", NewAlbumsModule)
}
