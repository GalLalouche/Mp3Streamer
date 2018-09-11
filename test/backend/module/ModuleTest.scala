package backend.module

import com.google.inject.util.Modules
import common.concurrency.SingleThreadedJobQueue
import net.codingwell.scalaguice.ScalaModule

import scala.concurrent.ExecutionContext

class ModuleTest extends ConfigurationModuleTestSpec {
  allRequiredBindingsSatisfied("Standalone", StandaloneModule)
  allRequiredBindingsSatisfied("CleanModule", CleanModule)
  allRequiredBindingsSatisfied("ControllerUtils", Modules.combine(new controllers.Module, new ScalaModule {
    override def configure() = {
      // Execution context is provided by Play at runtime.
      bind[ExecutionContext] toInstance new SingleThreadedJobQueue("DummyExecutionContext").asExecutionContext
    }
  }))
}
