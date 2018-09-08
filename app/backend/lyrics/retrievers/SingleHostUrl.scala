package backend.lyrics.retrievers

import models.Song

trait SingleHostUrl {
  def hostPrefix: String
  def urlFor(s: Song): String
}
