package backend.lyrics.retrievers

import models.Song

private trait SingleHostUrl {
  def hostPrefix: String
  def urlFor(s: Song): String
}
