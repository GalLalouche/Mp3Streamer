package musicfinder

import models.{AlbumDir, MemorySong}

import scala.collection.mutable

import common.io.{FileRef, MemoryDir, MemoryFile, MemorySystem}
import common.rich.RichT._

private class FakeMusicFilesImpl(
    _baseDir: MemoryDir,
    override val genresWithSubGenres: Seq[String],
    override val flatGenres: Seq[String],
) extends MusicFilesImpl[MemorySystem](_baseDir, FakeSongFileFinder)
    with FakeMusicFiles {
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
  def copyAlbum(albumDir: AlbumDir): AlbumDir =
    albumDir.copy(dir = dirToAddSongsTo.addSubDir(albumDir.dir.name, albumDir.dir.lastModified))
  override def apply(f: FileRef): MemorySong = pathToSongs(f.path)
}
object FakeMusicFilesImpl {
  def apply(
      baseDir: MemoryDir,
      genresWithSubGenres: Seq[String] = Vector("music"),
      flatGenres: Seq[String] = Nil,
  ): FakeMusicFiles = new FakeMusicFilesImpl(baseDir, genresWithSubGenres, flatGenres)
}
