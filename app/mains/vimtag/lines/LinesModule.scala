package mains.vimtag.lines

import mains.vimtag.{CommandsProvider, IndividualInitializer, IndividualParser}
import net.codingwell.scalaguice.ScalaModule

private[vimtag] object LinesModule extends ScalaModule {
  override def configure(): Unit = {
    bind[IndividualInitializer].toInstance(LinesInitializer)
    bind[CommandsProvider].toInstance(LinesCommandsProvider)
    bind[IndividualParser].toInstance(LinesParser)
  }
}
