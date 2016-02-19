package search

import models.Song

trait IndexBuilder {
  def buildIndexFor(songs: TraversableOnce[Song]): Index
}