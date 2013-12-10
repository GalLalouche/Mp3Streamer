package mains

// downloads from zi internet!
object FolderFixer extends App {
	try {
		val folder: String = args(0)
		val newFolder = FixLabels.fix(folder)
		DownloadCover.main(List(newFolder).toArray)
		println("Done!")
		readLine
	} catch {
		case e: Exception => e.printStackTrace(); readLine
	}
}