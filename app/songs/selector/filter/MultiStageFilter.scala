package songs.selector.filter

import backend.recon.Track
import models.Song

import common.Filter
import common.path.ref.FileRef

private[selector] trait MultiStageFilter {
  def fileFilter: Filter[FileRef]
  def trackFilter: Filter[Track]
  def songFilter: Filter[Song]
}
