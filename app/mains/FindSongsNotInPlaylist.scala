package mains

import java.io.File
import scala.collection.TraversableOnce.MonadOps
import java.nio.charset.StandardCharsets
import common.Debug
import common.rich.path.Directory
import common.rich.path.RichFile._
import loggers.ConsoleLogger
import models.MusicFinder
import common.rich.RichT.richT
import org.apache.commons.codec.binary.Base64

// finds songs that are in the music directory but are not saved in the playlist file
object FindSongsNotInPlaylist extends App with Debug {
	private def base64(s: String) =
		Base64.encodeBase64(s.getBytes(StandardCharsets.UTF_8)).map(_.toChar).mkString
	val real = new MusicFinder {
		val dir = Directory("D:/Media/Music")
		val subDirs = List("Metal", "Rock", "Classical", "New Age", "Jazz")
		val extensions = List("mp3", "flac", "ape", "wma", "mp4", "wav", "aiff", "aac", "ogg")
	}
	timed(logger = ConsoleLogger) {

		val playlistSongs = real.dir.addFile("playlist.m3u8")
			.lines
			// removes UTF-BOM at least until I fix it in scala common
			.log(_.head.getBytes.toList)
			.mapIf(_.head.head.toInt == 65279).to(e => e.tail :+ e.head.drop(1))
			.map(real.dir.path + "/" + _)
			.map(_.toLowerCase.replaceAll("\\\\", "/"))
			.toSet
		println(s"playlist songs |${playlistSongs.size}|")
		val realSongs = real.getSongFilePaths
			.map(_.toLowerCase.replaceAll("\\\\", "/"))
			.toSet
		println(s"actual songs |${realSongs.size}|")
		val playlistMissing = realSongs.diff(playlistSongs).toList.sorted
		//		playlistMissing // opens the windows with the files :D
		//			.map(new File(_).parent.getAbsolutePath)
		//			.toSet[String]
		//			.foreach(e => new ProcessBuilder("explorer.exe", e).start)
		val serverMissing = playlistSongs.diff(realSongs).toList.sorted
		println(s"Server is missing ${serverMissing.size} songs ${"(it's possible that these songs are in the playlist but the files themselves have been deleted)".onlyIf(serverMissing.nonEmpty)}")
		println(serverMissing.mkString("\n"))
		println(s"Playlist is missing ${playlistMissing.size} songs")
		println(playlistMissing.mkString("\n"))
	}
}