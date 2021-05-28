package mains.vimtag

import mains.vimtag.TableVimEdit.Command

private trait CommandsProvider {
  def width: Int
  def get: Seq[Command]
}
