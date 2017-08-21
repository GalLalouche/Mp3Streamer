package mains.cover

import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.{Files, StandardCopyOption}
import javax.imageio.ImageIO
import javax.swing.ImageIcon

import common.io.{FileRef, IOFile}
import common.rich.path.Directory

private case class FolderImage(file: FileRef) {
  private lazy val image = ImageIO.read(file.asInstanceOf[IOFile].file)
  def width = image.getWidth
  def height = image.getHeight
  def toIcon(width: Int, height: Int) = {
    val $ = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    val graphics = $.createGraphics
    graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
    graphics.drawImage(image, 0, 0, width, height, null)
    graphics.dispose()
    new ImageIcon($)
  }
	def move(to: Directory) {
		Files.move(file.asInstanceOf[IOFile].file.toPath,
			new File(to, "folder.jpg").toPath,
			StandardCopyOption.REPLACE_EXISTING)
	}

}
