package mains.vimtag.lines

import mains.vimtag.CommandsProvider

private object LinesCommandsProvider extends CommandsProvider {
  override def width = 120
  override def get = Nil
}
