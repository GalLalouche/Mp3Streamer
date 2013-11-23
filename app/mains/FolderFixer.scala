package mains

// downloads from zi internet!
object FolderFixer extends App {
	try {
		val folder: String = args(0)
		DownloadCover.main(List(folder).toArray)
		FixLabels.main(List(folder).toArray)
		println("Done!")
		readLine
	} catch {
		case e => e.printStackTrace(); readLine
	}
}