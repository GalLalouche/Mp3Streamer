package decoders

import java.io.File
import java.nio.file.Files

import scala.sys.process.Process

import common.rich.path.Directory
import common.rich.path.RichFile.richFile
import common.rich.path.RichPath.poorPath

object CueTools extends CueSplitter {
	private val exe = """C:\Program Files (x86)\CUETools_2.1.5\CUETools.exe""";
	private def clean(dir: Directory) {
		dir
			.files
			.filter(_.extension == "flac")
			.foreach(f => Files.move(f.toPath, new File(dir.getParentFile, f.name).toPath)) // move files up
		dir.deleteAll
	}
	def split(cueFile: File) {
		Process(exe, Seq("/convert", s""""${cueFile.getAbsolutePath}"""")).!!
		clean(Directory(cueFile.getParent) / "convert" /)
	}
}
