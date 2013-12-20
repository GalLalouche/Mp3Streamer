package models

import java.io.{ ByteArrayOutputStream, File, FileInputStream, InputStream }
import java.net.URL
import common.path.RichFile._
import javax.imageio.{ IIOImage, ImageIO, ImageWriteParam }
import javax.imageio.stream.FileImageOutputStream
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.awt.image.BufferedImage
import javax.imageio.stream.MemoryCacheImageOutputStream
import java.io.FileOutputStream
import java.awt.Color

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
	def apply(url: String) = {
		val in = scala.io.Source.fromInputStream(new URL(url).openConnection.getInputStream)
		val f = File.createTempFile("image", "tempfile")
		val out = new java.io.PrintWriter(f)
		try {
			in.getLines().foreach(out.print(_))
			new Image(f)
		} finally { out.close }
	}
	def apply(f: File) = new Image(f)
}