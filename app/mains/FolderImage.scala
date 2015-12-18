package mains

import javax.swing.ImageIcon

private case class FolderImage(file: java.io.File) {
	def imageIcon = new ImageIcon(file.getAbsolutePath)
}