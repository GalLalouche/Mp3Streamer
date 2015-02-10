package decoders

import java.io.File
import scala.sys.process.Process
import common.rich.RichAll._
import common.rich.path.Directory
import java.nio.file.Files

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
		val response = Process(exe, Seq("/convert", cueFile.getAbsolutePath.mapTo("\"" + _ + "\""))).!!
		clean(Directory(cueFile.getParent) / "convert" /)
	}
}