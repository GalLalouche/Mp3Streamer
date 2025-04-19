package mains.vimtag.table

import mains.vimtag.CommandsProvider
import mains.vimtag.VimEdit.{ExecutionCommand, NormalCommand}

private object TableCommandsProvider extends CommandsProvider {
  override val width = 240
  override val get = Vector(
    NormalCommand("G"), // Go to the end of the file
    ExecutionCommand("TableModeRealign"), // The realign has to invoked at the end for some reason
    ExecutionCommand("silent!TableModeEnable"), // If not silent, it'll just echo it annoyingly
  )
}
