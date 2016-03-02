package mains

import java.io.File
import java.nio.charset.StandardCharsets

import common.Debug
import common.rich.RichT.richT
import common.rich.path.Directory
import common.rich.path.RichFile._
import loggers.ConsoleLogger
import models.MusicFinder
import org.apache.commons.codec.binary.Base64

// finds songs that are in the music directory but are not saved in the playlist file
object FindSongsNotInPlaylist extends App with Debug {
	private def base64(s: String) =
		Base64.encodeBase64(s.getBytes(StandardCharsets.UTF_8)).map(_.toChar).mkString
	val musicFiles = new MusicFinder {
		val dir = Directory("D:/Media/Music")
		val subDirs = List("Metal", "Rock", "Classical", "New Age", "Jazz")
		val extensions = List("mp3", "flac", "ape", "wma", "mp4", "wav", "aiff", "aac", "ogg")
	}
	timed(logger = ConsoleLogger) {
		val playlistSongs = musicFiles.dir.addFile("playlist.m3u8") // UTF-8 helps deal with Hebrew songs
			.lines
			// removes UTF-BOM at least until I fix it in scala common
			.mapIf(_.head.head.toInt == 65279).to(e => e.tail :+ e.head.drop(1))
			.map(musicFiles.dir.path + "/" + _)
			.map(_.toLowerCase.replaceAll("\\\\", "/"))
			.toSet
		println(s"playlist songs |${playlistSongs.size}|")
		val realSongs = musicFiles.getSongFilePaths
			.map(_.toLowerCase.replaceAll("\\\\", "/"))
			.toSet
		println(s"actual songs |${realSongs.size}|")
		val playlistMissing = realSongs.diff(playlistSongs).toList.sorted
		playlistMissing // opens the windows with the files
			.map(new File(_).parent.getAbsolutePath).toSet[String]
			.foreach(new ProcessBuilder("explorer.exe", _).start)
		val serverMissing = playlistSongs.diff(realSongs).toList.sorted
		println(s"Server is missing ${serverMissing.size} songs. ${"It's possible that these songs are in the playlist but the files themselves have been deleted, or were added from a different folder, e.g., bittorent.".onlyIf(serverMissing.nonEmpty)}")
		println(serverMissing.mkString("\n"))
		println(s"Playlist is missing ${playlistMissing.size} songs")
		println(playlistMissing.mkString("\n"))
	}
}
