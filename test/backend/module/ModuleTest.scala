package backend.module

import backend.pkg.DownloaderController
import controllers.ControllerUtils

class ModuleTest extends ConfigurationModuleTestSpec {
  allRequiredBindingsSatisfied("Standalone", StandaloneModule)
  allRequiredBindingsSatisfied("CleanModule", CleanModule)
  allRequiredBindingsSatisfied("ControllerUtils", ControllerUtils.module)
  allRequiredBindingsSatisfied("DownloaderController", DownloaderController.injector)
}
