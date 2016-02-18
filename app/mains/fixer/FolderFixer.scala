package mains.fixer

import java.io.File
import java.nio.file.Files

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.sys.process.Process

import common.rich.RichT.richT
import common.rich.path.Directory
import common.rich.path.RichFile.richFile
import common.rich.path.RichPath.poorPath
import mains.cover.DownloadCover
import mains.cover.DownloadCover.CoverException
import models.Song

object FolderFixer {
	private def findArtistFolder(artist: String): Option[Directory] = {
		println("finding matching folder")
		Directory("d:/media/music")
			.deepDirs
			.find(_.name.toLowerCase == artist.toLowerCase)
	}

	private def moveDirectory(artist: String, destination: Future[Option[Directory]],
		folderImage: Future[Directory => Unit], sourcePath: String) {
		Await.result(folderImage, 1 minute).apply(Directory(sourcePath))
		if (destination.isCompleted == false)
			println("Waiting on artist find...")
		val destinationParent: Directory = Await.result(destination, 1 minute).getOrElse {
			val genre = readLine("Could not find artist directory... what is the artist's genre?\n").toLowerCase
			Directory("d:/media/music")
				.dirs
				.view
				.flatMap(_.dirs)
				.find(_.name.toLowerCase == genre)
				.get
				.addSubDir(artist)
		}
		val source = Directory(sourcePath)
		val dest = new File(destinationParent, source.name).toPath
		Files.move(source.toPath, dest)
		new ProcessBuilder("explorer.exe", dest.toFile.getAbsolutePath).start
	}

	private def downloadCover(newPath: Directory): Future[Directory => Unit] = {
		try
			DownloadCover.apply(newPath)
		catch {
			case CoverException(text, e) =>
				e.printStackTrace()
				println("Could not auto-download picture :( opening browser")
				Process("""C:\Users\Gal\AppData\Local\Google\Chrome\Application\chrome.exe "https://www.google.com/search?espv=2&biw=1920&bih=955&tbs=isz%3Aex%2Ciszw%3A500%2Ciszh%3A500&tbm=isch&sa=1&q=png """ + text).!!
				Future({ x: Any => () })
		}
	}

	def main(args: Array[String]) {
		def extractArtistFromFile(folder: Directory): String = folder
			.files
			.filter(Set("mp3", "flac") contains _.extension)
			.head
			.mapTo(Song.apply)
			.artist
		val folder = Directory(args(0))
		val artist = extractArtistFromFile(folder)
		val location = Future(findArtistFolder(artist))
		
		val folderImage = downloadCover(folder)
		println("fixing directory")
		val fixedDirectory = FixLabels fix folder.cloneDir()
		moveDirectory(artist, location, folderImage, fixedDirectory)
		
		println("--Done!--")
	}
}