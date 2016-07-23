package backend.external

import backend.storage.Retriever
import models.Song

trait ExternalLinksProvider extends Retriever[Song, ExternalLinks]
