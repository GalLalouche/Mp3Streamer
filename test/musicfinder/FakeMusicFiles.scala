package musicfinder

import models.{AlbumDir, MemorySong, SongTagParser}

import common.io.FileRef

trait FakeMusicFiles extends MusicFiles with SongTagParser {
  def copySong(s: MemorySong): MemorySong
  /** Adds a song under the requested directory names. */
  def copySong(dirName: String, s: MemorySong): MemorySong
  /** Adds a song under the requested directory name. */
  def copySong(path: Seq[String], s: MemorySong): MemorySong
  def copyAlbum(albumDir: AlbumDir): AlbumDir
  override def apply(f: FileRef): MemorySong
}
