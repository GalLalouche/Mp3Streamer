package mains

import scala.sys.process.Process
import DownloadCover.CoverException

// downloads from zi internet!
object FolderFixer extends App {
	try {
		val folder: String = args(0)
		val newFolder = FixLabels.fix(folder)
		try
			DownloadCover.main(List(newFolder).toArray)
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