package backend.module

import common.io.{FileRef, MemoryDir, MemoryFile, MemorySystem}
import common.rich.RichT._
import models.{ArtistName, MemorySong, SongTagParser}
import musicfinder.MusicFinder
import musicfinder.MusicFinder.DirectoryName
import scala.collection.mutable

class FakeMusicFinder(override val baseDir: MemoryDir) extends MusicFinder with SongTagParser {
  override type S = MemorySystem
  override val extensions = Set("mp3")
  override val unsupportedExtensions = Set()
  protected override def genresWithSubGenres: Seq[String] = Vector("music")
  override def flatGenres: Seq[String] = Nil
  override def genreDirs: Seq[MemoryDir] = super.genreDirs
  private val dirToAddSongsTo = baseDir.addSubDir(genresWithSubGenres.head)
  private val pathToSongs = mutable.HashMap[String, MemorySong]()

  private def copy(s: MemorySong, newFile: MemoryFile) =
    s.copy(file = newFile).<|(pathToSongs += newFile.path -> _)

  /**
   * Adds a song under root / songs / $artist_name / $album_time / $file_name. Ensures the song's
   * file matches the music finder directory structure.
   */
  def copySong(s: MemorySong): MemorySong = copySong(Vector(s.artistName, s.albumName), s)
  /** Adds a song under the requested directory names. */
  def copySong(dirName: String, s: MemorySong): MemorySong = copySong(Vector(dirName), s)
  /** Adds a song under the requested directory name. */
  def copySong(path: Seq[String], s: MemorySong): MemorySong =
    copy(s, path.foldLeft(dirToAddSongsTo)(_ addSubDir _).addFile(s.file.name))
  override def apply(f: FileRef): MemorySong = pathToSongs(f.path)
  protected override def normalizeArtistName(name: ArtistName): DirectoryName = name
}
