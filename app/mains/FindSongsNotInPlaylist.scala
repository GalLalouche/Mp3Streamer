package mains

import java.io.File

import scala.collection.TraversableOnce.MonadOps

import common.Debug
import common.path.Directory
import common.path.RichFile.richFile
import loggers.ConsoleLogger
import models.MusicFinder

// finds songs that are in the music directory but are not saved in the playlist file
object FindSongsNotInPlaylist extends App with Debug {
	val real = new MusicFinder {
		val dir = Directory("D:/Media/Music")
		val subDirs = List("Metal", "Rock", "Classical", "New Age", "Jazz")
		val extensions = List("mp3", "flac", "ape", "wma", "mp4", "wav", "aiff", "aac", "ogg")
	}
	timed(logger = ConsoleLogger) {

		val playlistSongs = new File((real.dir / "Playlist.m3u").path)
			.lines
			.map(real.dir.path + "/" + _)
			.map(_.toLowerCase.replaceAll("\\\\", "/"))
			.toSet
		println(s"playlist songs |${playlistSongs.size}|")
		val realSongs = real.getSongFilePaths
			.map(_.toLowerCase.replaceAll("\\\\", "/"))
			.toSet
		println(s"actual songs |${realSongs.size}|")

		val playlistMissing = realSongs.diff(playlistSongs).toList.sorted
		playlistMissing // opens the windows with the files :D
			.map(new File(_).parent.getAbsolutePath)
			.toSet[String]
			.foreach(e => new ProcessBuilder("explorer.exe", e).start)
		val serverMissing = playlistSongs.diff(realSongs).toList.sorted
		println(s"Server is missing ${serverMissing.size} songs ${if (serverMissing.nonEmpty) "(it's possible that these songs are in the playlist but the files themselves have been deleted)"}")
		println(serverMissing.mkString("\n"))
		println(s"Playlist is missing ${playlistMissing.size} songs")
		println(playlistMissing.mkString("\n"))
	}
}