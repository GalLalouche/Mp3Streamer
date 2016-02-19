package models

import java.io.{ ByteArrayOutputStream, File, FileInputStream, InputStream }
import java.net.URL
import common.rich.path.RichFile._
import javax.imageio.{ IIOImage, ImageIO, ImageWriteParam }
import javax.imageio.stream.FileImageOutputStream
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.awt.image.BufferedImage
import javax.imageio.stream.MemoryCacheImageOutputStream
import java.io.FileOutputStream
import java.awt.Color
import java.nio.file.Files

class Image(imageFile: File) {
	def saveAsJpeg(outFile: File): File = {
		val bufferedImage = ImageIO read imageFile;
		val newBufferedImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
		newBufferedImage.createGraphics().drawImage(bufferedImage, 0, 0, Color.WHITE, null);
		ImageIO.write(newBufferedImage, "jpg", outFile);
		outFile
	}
}

object Image {
	def apply(url: String): Image = {
		val f = File.createTempFile("image", "tempfile")
		f.deleteOnExit
		Files.copy(new URL(url).openConnection.getInputStream, f.toPath)
		new Image(f)
	}
	def apply(f: File): Image = new Image(f)
}