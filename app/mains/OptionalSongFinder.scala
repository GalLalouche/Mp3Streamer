package mains

import java.io.File

import com.google.inject.{ImplementedBy, Inject}
import musicfinder.MusicFinder

import common.io.DirectoryRef

@ImplementedBy(classOf[OptionalSongFinder.OptionalSongFinderImpl])
private trait OptionalSongFinder {
  def apply(d: DirectoryRef): Iterator[OptionalSong]
}
private object OptionalSongFinder {
  class OptionalSongFinderImpl @Inject() (mf: MusicFinder) extends OptionalSongFinder {
    override def apply(d: DirectoryRef): Iterator[OptionalSong] =
      mf.getSongFilesInDir(d).map(e => OptionalSongTagParser(new File(e.path)))
  }
}
