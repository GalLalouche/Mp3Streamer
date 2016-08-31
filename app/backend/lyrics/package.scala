package backend

import models.Song

import scala.concurrent.Future

package object lyrics {
  type LyricsRetriever = Retriever[Song, Lyrics]
}
