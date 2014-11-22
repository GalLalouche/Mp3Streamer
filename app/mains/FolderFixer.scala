package mains

import scala.sys.process.Process
import DownloadCover.CoverException
import common.rich.path.Directory
import common.rich.path.RichFile._
import common.rich.RichT._
import models.Song
import controllers.MusicLocations
import java.io.File

// downloads from zi internet!
object FolderFixer extends App {
	def findArtistFolder(folder: Directory): Option[Directory] = {
		val artist = folder
			.files
			.filter(f => Set("mp3", "flac").contains(f.extension))
			.head
			.mapTo(Song.apply)
			.artist
		Directory("d:/media/music")
			.deepDirs
			.find(_.name == artist)
	}
	try {
		val folder = Directory(args(0))
		val location = findArtistFolder(folder)
		println("copying directory")
		val clone = folder.cloneDir()
		val outputDir: Directory = location
			.map(new File(_, clone.name))
			.map { e => clone.dir.renameTo(e); e }
			.map(Directory.apply)
			.getOrElse(clone)
		println("fixing labels")
		val newPath = FixLabels fix outputDir
		for (dir <- location)
			new ProcessBuilder("explorer.exe", dir.getAbsolutePath()).start
		try
			DownloadCover.main(List(newPath).toArray)
		catch {
			case CoverException(text) =>
				println("Could not auto-download picture :( press any key to open browser")
				readLine
				Process("""C:\Users\Gal\AppData\Local\Google\Chrome\Application\chrome.exe "https://www.google.com/search?espv=2&biw=1920&bih=955&tbs=isz%3Aex%2Ciszw%3A500%2Ciszh%3A500&tbm=isch&sa=1&q=lastfm """ + text).!!
		}
	} catch {
		case e: Throwable =>
			e.printStackTrace()
			readLine
	}
}