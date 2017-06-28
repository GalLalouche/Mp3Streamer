package backend.configs

import common.io.{MemoryDir, MemoryFile, MemorySystem}
import models.{MemorySong, MusicFinder}

import scala.collection.mutable

class FakeMusicFinder(val dir: MemoryDir) extends MusicFinder {
  override type S = MemorySystem
  override val extensions = Set("mp3")
  protected override def subDirNames: List[String] = List("music")
  private val dirToAddSongsTo = dir addSubDir subDirNames.head
  private val pathToSongs = mutable.HashMap[String, MemorySong]()
  /** Adds a song under root / songs / $artist_name / $album_time / $file_name. */
  def addSong(s: MemorySong): MemoryDir = {
    val $ = dirToAddSongsTo addSubDir s.artistName addSubDir s.albumName
    val file = $ addFile s.file.name
    pathToSongs += file.path -> s
    $
  }
  override protected def parseSong(f: MemoryFile): MemorySong = pathToSongs(f.path)
}
