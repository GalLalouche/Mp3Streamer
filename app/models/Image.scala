package models

import java.net.URL
import common.io.RichStream._
import common.path.RichFile._
import javax.imageio.ImageIO
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.File
import common.path.Directory
import javax.imageio.ImageWriteParam
import javax.imageio.stream.FileImageOutputStream
import javax.imageio.IIOImage
import java.io.FileInputStream

class Image(is: InputStream) {
	def saveAsJpeg(f: File): File = {
		val baos = new ByteArrayOutputStream
		val writer = ImageIO.getImageWritersByFormatName("jpeg").next
		val iwp = writer.getDefaultWriteParam;
		iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT)
		iwp.setCompressionQuality(0.98f)

		f.createNewFile
		f.clear
		val output = new FileImageOutputStream(f);
		val image = new IIOImage(ImageIO.read(is), null, null)
		writer.setOutput(output)
		writer.write(null, image, iwp);
		writer.dispose();
		println("jpg size is: " + f.length / 1024)
		f
	}
}

object Image {
	def apply(url: String) = new Image(new URL(url).openConnection.getInputStream)
	def apply(f: File) = new Image(new FileInputStream(f))
}