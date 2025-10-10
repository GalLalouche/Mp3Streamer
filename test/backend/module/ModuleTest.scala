package backend.module

import com.google.inject.util.Modules
import common.concurrency.DaemonExecutionContext
import net.codingwell.scalaguice.ScalaModule
import scala.concurrent.ExecutionContext

class ModuleTest extends ConfigurationModuleTestSpec {
  allRequiredBindingsSatisfied("Standalone", StandaloneModule)
  allRequiredBindingsSatisfied("CleanModule", CleanModule)
  allRequiredBindingsSatisfied(
    "FormatterModule",
    Modules.combine(
      new formatter.FormatterModule(level = None),
      new ScalaModule {
        override def configure() =
          // Execution context Will be provided at runtime by the runtime module (play/http4s).
          bind[ExecutionContext].toInstance(DaemonExecutionContext.single("DummyExecutionContext"))
      },
    ),
  )
}
