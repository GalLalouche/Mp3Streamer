import models.Song

import scala.concurrent.Future

package object lyrics {
  type LyricsRetriever = (Song => Future[Lyrics])
}
