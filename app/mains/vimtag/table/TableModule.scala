package mains.vimtag.table

import mains.vimtag.{CommandsProvider, IndividualInitializer, IndividualParser}
import net.codingwell.scalaguice.ScalaModule

private[vimtag] object TableModule extends ScalaModule {
  override def configure(): Unit = {
    bind[IndividualInitializer].toInstance(TableInitializer)
    bind[CommandsProvider].toInstance(TableCommandsProvider)
    bind[IndividualParser].toInstance(TableParser)
  }
}
