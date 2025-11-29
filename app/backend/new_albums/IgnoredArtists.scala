package backend.new_albums

import com.google.inject.ImplementedBy

import common.io.DirectoryRef

@ImplementedBy(classOf[DirectoryDiscovery])
trait IgnoredArtists {
  def shouldIgnore(dir: DirectoryRef): Boolean
}
