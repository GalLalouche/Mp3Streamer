package mains

// downloads from zi internet!
object FolderFixer extends App {
	try {
		val folder: String = args(0)
		val newFolder = FixLabels.fix(folder)
		try {
			DownloadCover.main(List(newFolder).toArray)
			println("Done!")
		} catch { case e: Exception => println("Could not auto-download picture :(") }
	} catch {
		case e: Exception => e.printStackTrace()
	}
	readLine
}