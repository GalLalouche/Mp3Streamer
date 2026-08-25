package musicfinder

import models.{AlbumDir, MemorySong, SongTagParser}

import common.path.ref.FileRef
import common.test.memory_ref.MemoryDir

trait FakeMusicFiles extends MusicFiles with SongTagParser {
  override def baseDir: MemoryDir
  def copySong(s: MemorySong): MemorySong
  /** Adds a song under the requested directory names. */
  def copySong(dirName: String, s: MemorySong): MemorySong
  /** Adds a song under the requested directory name. */
  def copySong(path: Seq[String], s: MemorySong): MemorySong
  def copyAlbum(albumDir: AlbumDir): AlbumDir
  override def apply(f: FileRef): MemorySong
}
