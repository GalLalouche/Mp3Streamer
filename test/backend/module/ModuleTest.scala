package backend.module

import com.google.inject.util.Modules
import net.codingwell.scalaguice.ScalaModule

import scala.concurrent.ExecutionContext

import common.concurrency.DaemonExecutionContext

class ModuleTest extends ConfigurationModuleTestSpec {
  allRequiredBindingsSatisfied("Standalone", StandaloneModule)
  allRequiredBindingsSatisfied("CleanModule", CleanModule)
  allRequiredBindingsSatisfied(
    "ControllerUtils",
    Modules.combine(
      new controllers.Module(level = None),
      new ScalaModule {
        override def configure() =
          // Execution context is provided by Play at runtime.
          bind[ExecutionContext].toInstance(DaemonExecutionContext.single("DummyExecutionContext"))
      },
    ),
  )
}
