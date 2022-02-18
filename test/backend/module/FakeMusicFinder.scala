package backend.module

import models.{MemorySong, MusicFinder}

import scala.collection.mutable

import common.io.{DirectoryRef, FileRef, MemoryDir, MemoryFile, MemorySystem}
import common.rich.RichT._

class FakeMusicFinder(val baseDir: MemoryDir) extends MusicFinder {
  override type S = MemorySystem
  override val extensions = Set("mp3")
  override protected def genresWithSubGenres: Seq[String] = Vector("music")
  override def flatGenres: Seq[String] = Nil
  private val dirToAddSongsTo = baseDir addSubDir genresWithSubGenres.head
  private val pathToSongs = mutable.HashMap[String, MemorySong]()

  private def copy(s: MemorySong, newFile: MemoryFile) =
    s.copy(file = newFile).<|(pathToSongs += newFile.path -> _)

  /**
  * Adds a song under root / songs / $artist_name / $album_time / $file_name.
  * Ensures the song's file matches the music finder directory structure.
  */
  def copySong(s: MemorySong): MemorySong =
    copy(s, dirToAddSongsTo addSubDir s.artistName addSubDir s.albumName addFile s.file.name)
  /** Adds a song under the requested directory name. */
  def copySong(dirName: String, s: MemorySong): MemorySong =
    copy(s, dirToAddSongsTo.addSubDir(dirName).addFile(s.file.name))
  override def parseSong(f: FileRef): MemorySong = pathToSongs(f.path)
  override def getOptionalSongsInDir(d: DirectoryRef) =
    d.files.map(_.path).map(pathToSongs).map(_.toOptionalSong)
}
