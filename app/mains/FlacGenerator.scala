package mains
import common.rich.RichAll._
import decoders.CueTools

//splits cue file and fixes the flac output
object FlacGenerator {
	def main(args: Array[String]) {
		val splitter = CueTools
		splitter.split("""D:\Incoming\Bittorrent\Completed\Music\Rotting Christ LOSSLESS\[1994] [CD] Non Serviam [EAC-FLAC]\Rotting Christ - (1994) Non Serviam.flac.cue""")
	}
}