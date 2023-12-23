package backend.lyrics

import common.storage.Storage
import models.Song

private trait LyricsStorage extends Storage[Song, Lyrics]
