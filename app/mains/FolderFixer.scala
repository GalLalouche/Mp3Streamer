package mains

// downloads from zi internet!
object FolderFixer extends App {
	try {
		val folder: String = args(0)
		val newFolder = FixLabels.fix(folder)
		try { DownloadCover.main(List(newFolder).toArray) } catch { case e: Exception => throw new Exception("Could not download a cover picture", e) }
		println("Done!")
		readLine
	} catch {
		case e: Exception => e.printStackTrace(); readLine
	}
}