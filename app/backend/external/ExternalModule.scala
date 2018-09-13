package backend.external

import backend.external.expansions.ExpansionsModule
import backend.external.recons.ReconsModule
import net.codingwell.scalaguice.ScalaModule

object ExternalModule extends ScalaModule {
  override def configure(): Unit = {
    install(ExpansionsModule)
    install(ReconsModule)
  }
}
