package mains.vimtag

import java.io.File

import common.rich.path.RichFile.richFile
import common.rich.RichT.richT

/** Creates a file with preset bindings to toggle flags on and off. */
private object SetupBindings {
  def createFile(): File = {
    val file = File.createTempFile("vimedit_macros", ".vim").<|(_.deleteOnExit())
    val commentString = "set commentstring=#\\ %s" // Make # the comment token for vim commentary.
    val flagMacros =
      // Search flag, comment out/in, get back to previous cursor position.
      Flag.values.map(f => s"nmap <leader>${f.mnemonic} mb/${escapeName(f)}<CR>gcc`bmb")
    file.write((commentString +: flagMacros).mkString("\n"))
  }

  private def escapeName(flag: Flag): String = flag.flag.replace("<", "\\<").replace(">", "\\>")
}
