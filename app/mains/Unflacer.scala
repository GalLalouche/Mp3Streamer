package mains
import decoders.CueTools

//splits cue file and fixes the flac output
object Unflacer {
	def main(args: Array[String]) {
		try {
			CueTools.split(args(0))
		} catch {
			case e: Exception => e.printStackTrace()
		}
	}
}
