package mains

import java.io.File
import java.time.Duration

import common.rich.RichT.richT
import common.rich.func.ToMoreMonadPlusOps
import common.rich.path.RichFile._
import models.IOMusicFinder

import scalaz.std.VectorInstances

// finds songs that are in the music directory but are not saved in the playlist file
object FindSongsNotInPlaylist
    extends ToMoreMonadPlusOps with VectorInstances {
  private val musicFiles = new IOMusicFinder {
    override val extensions = Set("mp3", "flac", "ape", "wma", "mp4", "wav", "aiff", "aac", "ogg")
  }
  private val UtfBytemarkPrefix = 65279
  def main(args: Array[String]): Unit = {
    val file = musicFiles.dir.addFile("playlist.m3u8").file
    if (Duration.ofMillis(System.currentTimeMillis() - file.lastModified()).toHours > 1)
      throw new IllegalStateException("Please update the playlist file.")
    val playlistSongs = file // UTF-8 helps deal with Hebrew songs
        .lines
        .toList
        // removes UTF-BOM at least until I fix it in ScalaCommon
        .mapIf(_.head.head.toInt == UtfBytemarkPrefix).to(e => e.tail :+ e.head.drop(1))
        .mapIf(_.head == "#").to(_.tail) // Never version of Foobar2000 decided to add # to file header :\
        .map(musicFiles.dir.path.+("/").+)
        .map(_.toLowerCase.replaceAll("\\\\", "/"))
        .toSet
    println(s"playlist songs |${playlistSongs.size}|")
    val realSongs = musicFiles.getSongFiles
        .map(_.path.toLowerCase.replaceAll("\\\\", "/"))
        .toSet
    println(s"actual songs |${realSongs.size}|")
    val playlistMissing = realSongs.diff(playlistSongs).toVector.sorted
    playlistMissing // opens the windows with the files
        .map(new File(_).parent.dir)
        .uniqueBy(_.getAbsolutePath)
        .take(10)
        .foreach(IOUtils.focus)
    val serverMissing = playlistSongs.diff(realSongs).toList.sorted
    println(s"Server is missing ${serverMissing.size} songs. " +
        s"${
          ("It's possible that these songs are in the playlist but the files themselves have been deleted, " +
              "or were added from a different folder, e.g., bittorent.").onlyIf(serverMissing.nonEmpty)
        }")
    println(serverMissing mkString "\n")
    println(s"Playlist is missing ${playlistMissing.size} songs")
    println(playlistMissing mkString "\n")
    System exit 0
  }
}
