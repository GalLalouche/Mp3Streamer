package backend.new_albums

import com.google.inject.Inject
import musicfinder.MusicFiles
import rx.lang.scala.Observable

import scala.collection.View

import common.io.DirectoryRef

private class DirectoryDiscovery @Inject() (mf: MusicFiles) extends IgnoredArtists {
  private val IgnoredFolders = Vector("Classical", "Musicals")
  override def shouldIgnore(dir: DirectoryRef): Boolean = {
    val genrePrefix = dir.path.drop(prefixLength)
    IgnoredFolders.exists(genrePrefix.startsWith)
  }
  def artistDirectories: View[DirectoryRef] = artistDirectoriesTyped
  def albumDirectories: Observable[DirectoryRef] = mf.albumDirs(artistDirectoriesTyped)

  private def artistDirectoriesTyped: View[DirectoryRef] =
    mf.artistDirs.filterNot(shouldIgnore)
  private val prefixLength = {
    val $ = mf.baseDir.path
    $.length + (if ($.endsWith("\\") || $.endsWith("/")) 0 else 1)
  }
}
