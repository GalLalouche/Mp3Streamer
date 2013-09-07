package mains

import models.MusicFinder
import common.path.Directory
import java.io.File
import common.path.RichFile._
import org.joda.time.DateTime
import common.Debug
import loggers.ConsoleLogger
import loggers.ConsoleLogger

object FindSongsNotInPlaylist extends App with Debug {
	val real = new MusicFinder {
		val dir = Directory("D:/Media/Music")
		val subDirs = List("Metal", "Rock", "Classical", "New Age")
		val extensions = List("mp3", "flac", "ape", "wma", "mp4", "wav", "aiff", "aac", "ogg")
	}
	timed(logger = ConsoleLogger) {

		val playlistSongs = new File((real.dir / "Playlist.m3u").path)
			.lines
			.map(real.dir.path + "/" + _)
			.map(_.toLowerCase.replaceAll("\\\\", "/"))
			.toSet
		println("playlist song |%d|".format(playlistSongs.size))
		val realSongs = real.getSongs
			.map(_.toLowerCase.replaceAll("\\\\", "/"))
			.toSet
		println("playlist song |%d|".format(realSongs.size))

		val playlistMissing = realSongs.diff(playlistSongs).toList.sorted
		val serverMissing = playlistSongs.diff(realSongs).toList.sorted
		println("Server is missing %d songs".format(serverMissing.size))
		println(serverMissing.mkString("\n"))
		println("Playlist is missing %d songs".format(playlistMissing.size))
		println(playlistMissing.mkString("\n"))
	}
}