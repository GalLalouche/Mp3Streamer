package mains.cover

import java.io.File
import java.nio.file.{Files, StandardCopyOption}
import javax.swing.ImageIcon

import common.rich.path.Directory

private case class FolderImage(file: java.io.File) {
	def imageIcon = new ImageIcon(file.getAbsolutePath)
	def move(to: Directory) {
		Files.move(file.toPath, new File(to, "folder.jpg").toPath, StandardCopyOption.REPLACE_EXISTING)
	}
}
