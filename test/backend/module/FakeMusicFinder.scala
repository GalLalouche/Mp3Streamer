package backend.module

import models.{MemorySong, MusicFinder}

import scala.collection.mutable

import common.io.{FileRef, MemoryDir, MemorySystem}

class FakeMusicFinder(val dir: MemoryDir) extends MusicFinder {
  override type S = MemorySystem
  override val extensions = Set("mp3")
  override protected def genresWithSubGenres: Seq[String] = Vector("music")
  override protected def flatGenres: Seq[String] = Nil
  private val dirToAddSongsTo = dir addSubDir genresWithSubGenres.head
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
