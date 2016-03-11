package controllers

import common.io.IODirectory

trait MusicLocations {
	val dir = IODirectory("d:/media/music")
	val subDirs = List("Rock", "New Age", "Classical", "Metal")
	val extensions = List("mp3", "flac")
}
