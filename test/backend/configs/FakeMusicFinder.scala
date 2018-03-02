package backend.configs

import common.io.{FileRef, MemoryDir, MemorySystem}
import models.{MemorySong, MusicFinder}

import scala.collection.mutable

class FakeMusicFinder(val dir: MemoryDir) extends MusicFinder {
  override type S = MemorySystem
  override val extensions = Set("mp3")
  protected override def subDirNames: List[String] = List("music")
  private val dirToAddSongsTo = dir addSubDir subDirNames.head
  private val pathToSongs = mutable.HashMap[String, MemorySong]()

  /**
  * Adds a song under root / songs / $artist_name / $album_time / $file_name.
  * Ensures the song's file matches the music finder directory structure.
  */
  def copySong(s: MemorySong): MemorySong = {
    val newFile = dirToAddSongsTo addSubDir s.artistName addSubDir s.albumName addFile s.file.name
    val $ = s.copy(file = newFile)
    pathToSongs += newFile.path -> $
    $
  }
  override def parseSong(f: FileRef): MemorySong = pathToSongs(f.path)
}
