package backend.configs

import common.io.{DirectoryRef, MemoryDir, MemoryFile, MemorySystem}
import models.{MusicFinder, Song}

import scala.collection.mutable

class FakeMusicFinder(val dir: MemoryDir) extends MusicFinder {
  override type S = MemorySystem
  override val extensions = Set("mp3")
  protected override val subDirNames: List[String] = List("music")
  private val musicDir = dir addSubDir subDirNames.head
  private val pathToSongs = mutable.HashMap[String, Song]()
  /** Adds a song under root / songs / $artist_name / $album_time / $file_name. */
  def addSong(s: Song): MemoryDir = {
    val $ = musicDir addSubDir s.artistName addSubDir s.albumName
    val file = $ addFile s.file.name
    pathToSongs += file.path -> s
    $
  }
  override protected def parseSong(f: MemoryFile) = pathToSongs(f.path)
}
