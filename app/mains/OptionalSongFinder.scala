package mains

import java.io.File
import javax.inject.Inject

import com.google.inject.ImplementedBy
import musicfinder.MusicFinder

import common.io.DirectoryRef

@ImplementedBy(classOf[OptionalSongFinder.OptionalSongFinderImpl])
private trait OptionalSongFinder {
  def apply(d: DirectoryRef): Seq[OptionalSong]
}
private object OptionalSongFinder {
  class OptionalSongFinderImpl @Inject() (mf: MusicFinder) extends OptionalSongFinder {
    override def apply(d: DirectoryRef): Seq[OptionalSong] =
      mf.getSongFilesInDir(d).map(e => OptionalSongTagParser(new File(e.path)))
  }
}
