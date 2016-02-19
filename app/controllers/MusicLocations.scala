package controllers

import common.rich.path.Directory

trait MusicLocations {
	val dir = Directory("d:/media/music")
	val subDirs = List("Rock", "New Age", "Classical", "Metal")
	val extensions = List("mp3", "flac")
}