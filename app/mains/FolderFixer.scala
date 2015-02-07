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

// downloads from zi internet!
object FolderFixer extends App {
	private def findArtistFolder(folder: Directory): Option[Directory] = {
		println("Searching for artist folder")
		val artist = folder
			.files
			.filter(f => Set("mp3", "flac").contains(f.extension))
			.head
			.mapTo(Song.apply)
			.artist
		Directory("d:/media/music")
			.deepDirs
			.find(_.name.toLowerCase == artist.toLowerCase)
	}
	private def moveDirectory(location: java.util.concurrent.Future[Option[common.rich.path.Directory]], newPath: String) {
		if (location.isDone() == false)
			println("Waiting on artist find...")
		for (d <- location.get()) {
			val newFile = new File(newPath)
			val moved = Files.move(newFile.toPath, new File(d, newFile.name).toPath)
			new ProcessBuilder("explorer.exe", d.getAbsolutePath).start
		}
	}

	private def downloadCover(newPath: String) {
		try
			DownloadCover.main(List(newPath).toArray)
		catch {
			case CoverException(text) =>
				println("Could not auto-download picture :( opening browser")
				Process("""C:\Users\Gal\AppData\Local\Google\Chrome\Application\chrome.exe "https://www.google.com/search?espv=2&biw=1920&bih=955&tbs=isz%3Aex%2Ciszw%3A500%2Ciszh%3A500&tbm=isch&sa=1&q=lastfm """ + text).!!
		}
	}
	private implicit def richCall[T](f: () => T): Callable[T] = new Callable[T] { override def call: T = f() }
	private val executors = Executors newFixedThreadPool 1
	try {
		val folder = Directory(args(0))
		val location = executors.submit(() => findArtistFolder(folder))
		println("fixing directory")
		val newPath = FixLabels fix folder.cloneDir()
		downloadCover(newPath)
		moveDirectory(location, newPath)
		println("--Done!--")
	} catch {
		case e: Throwable =>
			e.printStackTrace()
			readLine
	} finally
		executors.shutdownNow

}