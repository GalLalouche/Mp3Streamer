package controllers

import common.path.Directory

trait MusicLocations {
	val dir = Directory("d:/media/music")
	val subDirs = List("Rock", "New Age", "Classical")
	val extensions = List("mp3", "flac")
}