package mains

import java.io.File
import java.net.URL
import java.nio.file.{ Files, StandardCopyOption }
import scala.collection.JavaConversions.asScalaBuffer
import scala.swing.Dialog
import org.jsoup.Jsoup
import common.Debug
import common.rich.path.Directory
import common.rich.path.RichFile.{ poorFile, richFile }
import common.rich.primitives.RichString.richString
import javax.swing.ImageIcon
import models.Song
import java.nio.charset.MalformedInputException

// Uses google image search (not API, actual site) to find images
// Then displays the images for the user to select a good picture
object DownloadCover extends Debug {
	case class CoverException(str: String, e: Exception) extends Exception(e)
	lazy val tempFolder: Directory = Directory.apply(Files.createTempDirectory("images").toFile)

	def apply(folderPath: String) {
		val dir = Directory(folderPath);
		val outputFile = {
			val oldFile = dir \ "folder.jpg"
			if (oldFile.exists)
				oldFile.renameTo("folder.bak.jpg")
			dir.addFile("folder.jpg")
		}
		try {
			println("Looking online for images for folder: " + folderPath)
			val response = getHtml({
				val song = Song(dir.files.head)
				s"${song.artist} ${song.album}"
			})
			println("Images extracted, downloading and displaying images to user...")
			val selectedImage = selectImage(extractImages(response).toList)
			println("Selected " + selectedImage)
			Files.move(selectedImage.toPath, outputFile.toPath, StandardCopyOption.REPLACE_EXISTING)
			println("Image succesfully downloaded")
		} finally {
			tempFolder.deleteAll()
		}
	}

	private def createConnection(url: String) = {
		val $ = new URL(url).openConnection()
		$.setRequestProperty("user-agent", """user-agent:Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36""")
		$
	}

	private def getHtml(query: String): String = {
		val url = s"https://www.google.com/search?tbs=isz%3Aex%2Ciszw%3A500%2Ciszh%3A500&tbm=isch&q=$query"
			.replaceAll(" ", "%20")
		scala.io.Source.fromInputStream(createConnection(url).getInputStream, "UTF-8").getLines().mkString("\n")
	}

	private def extractImages(html: String): Seq[String] = {
		Jsoup.parse(html)
			.select("a")
			.map(_.attr("href"))
			.filterNot(_.isEmpty)
			.filter(_.matches(".*imgurl=.*"))
			.map(_.captureWith(".*imgurl=(.*?)\\&.*".r))
	}

	private def selectImage(imageUrls: Seq[String]): File = {
		downloadFile(imageUrls.head)
			.flatMap(displayImageAndGetResponse)
			.getOrElse(selectImage(imageUrls.tail))
	}

	private def displayImageAndGetResponse(f: File): Option[File] = {
		val img = new ImageIcon(f.getAbsolutePath)
		Dialog.showConfirmation(message = null, icon = img) match {
			case Dialog.Result.Yes => Some(f)
			case Dialog.Result.No => None
			case Dialog.Result.Closed => throw new Exception("User canceled the procedure")
		}
	}

	private def downloadFile(fileToDownload: String): Option[File] = {
		/*
		 * Tries to get the bytes with a list of encodings
		 * @param encodings The list of encodings to attempt 
		 */
		def getBytes(encodings: Seq[String]): Array[Byte] = {
			assert(encodings.nonEmpty, "Exhausted all encodings")
			try {
				scala.io.Source.fromInputStream(createConnection(fileToDownload).getInputStream, encodings.head)
					.map(_.toByte)
					.toArray;
			} catch {
				case e: MalformedInputException =>
					e.printStackTrace()
					getBytes(encodings.tail)
			}
		}
		try {
			val bytes = getBytes(List("UTF-8", "UTF-16", "ISO-8859-1", "Cp1252"))
			val out = tempFolder
				.addSubDir("images")
				.addFile(System.currentTimeMillis() + "img.jpg")
				.clear()
				.write(bytes)
			Some(out)
		} catch {
			case e: Exception =>
				println(s"Could not download $fileToDownload. Error: ${e.getMessage}")
				throw e
				None
		}
	}

}