package backend.lyrics

import common.storage.Storage
import models.Song

trait LyricsStorage extends Storage[Song, Lyrics]
