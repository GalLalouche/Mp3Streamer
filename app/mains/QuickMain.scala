package mains

import other.MusicBrainzRetriever
import common.rich.RichAll._

object QuickMain {
	def main(args: Array[String]) {
		MusicBrainzRetriever.getAlbums("Mother Mother").toVector.log()
	}
}