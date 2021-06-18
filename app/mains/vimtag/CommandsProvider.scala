package mains.vimtag

import mains.vimtag.VimEdit.Command

private trait CommandsProvider {
  def width: Int
  def get: Seq[Command]
}
