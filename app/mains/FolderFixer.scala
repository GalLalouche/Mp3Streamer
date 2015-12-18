package mains

import java.io.File
import java.nio.file.Files
import java.util.concurrent.{ Callable, Executors, Future }
import scala.sys.process.Process
import common.rich.RichT.richT
import common.rich.path.Directory
import common.rich.path.RichPath.poorPath
import common.rich.path.RichFile.richFile
import models.Song
import mains.cover.DownloadCover
import mains.cover.DownloadCover.CoverException

object FolderFixer {
	private def findArtistFolder(artist: String): Option[Directory] = {
		println("finding matching folder")
		Directory("d:/media/music")
			.deepDirs
			.find(_.name.toLowerCase == artist.toLowerCase)
	}

	private def moveDirectory(artist: String, destination: Future[Option[Directory]], sourcePath: String) {
		if (destination.isDone() == false)
			println("Waiting on artist find...")
		val d: Directory = destination.get().getOrElse {
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
		Files.move(source.toPath, new File(d, source.name).toPath)
		new ProcessBuilder("explorer.exe", d.getAbsolutePath).start
	}

	private def downloadCover(newPath: String) {
		try
			DownloadCover.apply(newPath)
		catch {
			case CoverException(text, e) =>
				e.printStackTrace()
				println("Could not auto-download picture :( opening browser")
				Process("""C:\Users\Gal\AppData\Local\Google\Chrome\Application\chrome.exe "https://www.google.com/search?espv=2&biw=1920&bih=955&tbs=isz%3Aex%2Ciszw%3A500%2Ciszh%3A500&tbm=isch&sa=1&q=png """ + text).!!
		}
	}

	def main(args: Array[String]) {
		def extractArtistFromFile(folder: Directory): String = folder
			.files
			.filter(Set("mp3", "flac") contains _.extension)
			.head
			.mapTo(Song.apply)
			.artist
		//FunctionalInterface workaround
		implicit def richCall[T](f: () => T): Callable[T] = new Callable[T] { override def call: T = f() }
		val executors = Executors newFixedThreadPool 1 // That's one way to create a future I guess...
		val folder = Directory(args(0))
		try {
			val artist = extractArtistFromFile(folder)
			val location = executors.submit(() => findArtistFolder(artist))
			println("fixing directory")
			val newPath = FixLabels fix folder.cloneDir()
			downloadCover(newPath)
			moveDirectory(artist, location, newPath)
			println("--Done!--")
		} catch {
			case e: Throwable =>
				e.printStackTrace()
				readLine()
		} finally
			executors.shutdownNow
	}
}