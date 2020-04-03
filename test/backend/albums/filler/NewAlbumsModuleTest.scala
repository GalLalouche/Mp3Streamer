package backend.albums.filler

import backend.module.ConfigurationModuleTestSpec

class NewAlbumsModuleTest extends ConfigurationModuleTestSpec {
  allRequiredBindingsSatisfied("NewAlbumsConfig", LocalNewAlbumsModule)
}
