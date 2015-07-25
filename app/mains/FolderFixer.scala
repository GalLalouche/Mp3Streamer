package mains

import java.io.File
import java.nio.file.Files
import java.util.concurrent.{ Callable, Executors }
import scala.sys.process.Process
import DownloadCover.CoverException
import common.rich.RichT.richT
import common.rich.path.Directory
import common.rich.path.RichFile.richFile
import common.rich.path.RichPath.poorPath
import models.Song
import java.util.concurrent.Future

// downloads from zi internet!
object FolderFixer {
	private def findArtistFolder(folder: Directory): Option[Directory] = {
		println("finding artist's name...")
		val artist = extractArtistFromFile(folder)
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
		val source = new File(sourcePath)
		Files.move(source.toPath, new File(d, source.name).toPath)
		new ProcessBuilder("explorer.exe", d.getAbsolutePath).start
	}

	private def downloadCover(newPath: String) {
		try
			DownloadCover.main(List(newPath).toArray)
		catch {
			case CoverException(text, e) =>
				e.printStackTrace()
				println("Could not auto-download picture :( opening browser")
				Process("""C:\Users\Gal\AppData\Local\Google\Chrome\Application\chrome.exe "https://www.google.com/search?espv=2&biw=1920&bih=955&tbs=isz%3Aex%2Ciszw%3A500%2Ciszh%3A500&tbm=isch&sa=1&q=png """ + text).!!
		}
	}

	//FunctionalInterface workaround
	
	def main(args: Array[String]) {
		implicit def richCall[T](f: () => T): Callable[T] = new Callable[T] { override def call: T = f() }
		val executors = Executors newFixedThreadPool 1
		try {
			val folder = Directory(args(0))
			val location = executors.submit(() => findArtistFolder(folder))
			println("fixing directory")
			val newPath = FixLabels fix folder.cloneDir()
			downloadCover(newPath)
			moveDirectory(extractArtistFromFile(folder), location, newPath)
			println("--Done!--")
		} catch {
			case e: Throwable =>
				e.printStackTrace()
				readLine()
		} finally
			executors.shutdownNow
	}

	private def extractArtistFromFile(folder: Directory): String = folder
		.files
		.filter(Set("mp3", "flac") contains _.extension)
		.head
		.mapTo(Song.apply)
		.artist
}