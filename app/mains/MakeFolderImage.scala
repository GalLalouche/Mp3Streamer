package mains

import common.rich.path.RichFile
import common.rich.path.RichPath
import models.Image
import java.io.File

// downloads from zi internet!
object MakeFolderImage extends App {
	try {
		val file = RichFile(args(0))
		if (file.extension != "jpg")
			Image(file).saveAsJpeg(new File(file.parent, "folder.jpg"))
		else
			file renameTo new File(file.parent, "folder.jpg")
	} catch {
		case e: Any => e.printStackTrace(); readLine
	}
}