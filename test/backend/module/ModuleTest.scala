package backend.module

import scala.concurrent.ExecutionContext

import com.google.inject.util.Modules
import common.concurrency.DaemonFixedPool
import net.codingwell.scalaguice.ScalaModule

class ModuleTest extends ConfigurationModuleTestSpec {
  allRequiredBindingsSatisfied("Standalone", StandaloneModule)
  allRequiredBindingsSatisfied("CleanModule", CleanModule)
  allRequiredBindingsSatisfied(
    "ControllerUtils",
    Modules.combine(
      new controllers.Module,
      new ScalaModule {
        override def configure() =
          // Execution context is provided by Play at runtime.
          bind[ExecutionContext].toInstance(DaemonFixedPool.single("DummyExecutionContext"))
      },
    ),
  )
}
