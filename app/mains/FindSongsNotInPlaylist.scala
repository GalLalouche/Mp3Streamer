package mains

import java.io.File

import common.rich.RichT.richT
import common.rich.path.RichFile._
import models.IOMusicFinder
import org.joda.time.Duration

// finds songs that are in the music directory but are not saved in the playlist file
object FindSongsNotInPlaylist {
  val musicFiles = new IOMusicFinder {
    override val extensions = Set("mp3", "flac", "ape", "wma", "mp4", "wav", "aiff", "aac", "ogg")
  }
  def main(args: Array[String]): Unit = {
    val file = musicFiles.dir.addFile("playlist.m3u8").file
    if (new Duration(System.currentTimeMillis() - file.lastModified()).getStandardHours > 1)
      throw new IllegalStateException("Please update the playlist file.")
    val playlistSongs = file // UTF-8 helps deal with Hebrew songs
        .lines
        // removes UTF-BOM at least until I fix it in ScalaCommon
        .mapIf(_.head.head.toInt == 65279).to(e => e.tail :+ e.head.drop(1))
        .map(musicFiles.dir.path + "/" + _)
        .map(_.toLowerCase.replaceAll("\\\\", "/"))
        .toSet
    println(s"playlist songs |${playlistSongs.size}|")
    val realSongs = musicFiles.getSongFiles
        .map(_.path.toLowerCase.replaceAll("\\\\", "/"))
        .toSet
    println(s"actual songs |${realSongs.size}|")
    val playlistMissing = realSongs.diff(playlistSongs).toList.sorted
    playlistMissing // opens the windows with the files
        .map(new File(_).parent.getAbsolutePath).toSet[String]
        .map(new File(_))
        .take(10)
        .foreach(IOUtils.focus)
    val serverMissing = playlistSongs.diff(realSongs).toList.sorted
    println(s"Server is missing ${serverMissing.size} songs. " +
        s"${("It's possible that these songs are in the playlist but the files themselves have been deleted, " +
            "or were added from a different folder, e.g., bittorent.").onlyIf(serverMissing.nonEmpty)}")
    println(serverMissing mkString "\n")
    println(s"Playlist is missing ${playlistMissing.size} songs")
    println(playlistMissing mkString "\n")
    System exit 0
  }
}
