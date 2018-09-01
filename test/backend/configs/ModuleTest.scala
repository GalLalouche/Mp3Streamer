package backend.configs

class ModuleTest extends ConfigurationModuleTestSpec {
  allRequiredBindingsSatisfied("StandaloneConfig", StandaloneModule)
  allRequiredBindingsSatisfied("CleanConfiguration", CleanModule)
}
