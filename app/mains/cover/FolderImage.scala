package mains.cover

import java.io.File
import java.nio.file.{Files, StandardCopyOption}
import javax.swing.ImageIcon

import common.io.{FileRef, IOFile}
import common.rich.path.Directory

private case class FolderImage(file: FileRef) {
	def move(to: Directory) {
		Files.move(file.asInstanceOf[IOFile].file.toPath,
			new File(to, "folder.jpg").toPath,
			StandardCopyOption.REPLACE_EXISTING)
	}
}
