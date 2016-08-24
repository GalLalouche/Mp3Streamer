package models

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import java.nio.file.Files
import javax.imageio.ImageIO

class Image(imageFile: File) {
	def saveAsJpeg(outFile: File): File = {
		val bufferedImage = ImageIO read imageFile
    val newBufferedImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB)
    newBufferedImage.createGraphics().drawImage(bufferedImage, 0, 0, Color.WHITE, null)
    ImageIO.write(newBufferedImage, "jpg", outFile)
    outFile
	}
}

object Image {
	def apply(url: String): Image = {
		val f = File.createTempFile("image", "tempfile")
		f.deleteOnExit()
		Files.copy(new URL(url).openConnection.getInputStream, f.toPath)
		new Image(f)
	}
	def apply(f: File): Image = new Image(f)
}
