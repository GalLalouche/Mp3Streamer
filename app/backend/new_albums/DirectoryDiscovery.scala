package backend.new_albums

import com.google.inject.Inject
import musicfinder.MusicFinder

import scala.collection.View

import common.io.DirectoryRef

private class DirectoryDiscovery @Inject() (mf: MusicFinder) extends IgnoredArtists {
  private type S = mf.S

  private val IgnoredFolders = Vector("Classical", "Musicals")
  override def shouldIgnore(dir: DirectoryRef): Boolean = {
    val genrePrefix = dir.path.drop(prefixLength)
    IgnoredFolders.exists(genrePrefix.startsWith)
  }
  def artistDirectories: View[DirectoryRef] = artistDirectoriesTyped
  def albumDirectories: View[DirectoryRef] = mf.albumDirs(artistDirectoriesTyped)

  private def artistDirectoriesTyped: View[S#D] = mf.artistDirs.filterNot(shouldIgnore)
  private val prefixLength = {
    val $ = mf.baseDir.path
    $.length + (if ($.endsWith("\\") || $.endsWith("/")) 0 else 1)
  }
}
